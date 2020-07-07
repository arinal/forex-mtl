# A local proxy for Forex rates

Build a local proxy for getting Currency Exchange Rates

**Requirements**

Forex is a simple application that acts as a local proxy for getting exchange rates.
It's a service that can be consumed by other internal services to get the exchange rate between a set of currencies,
so they don't have to care about the specifics of third-party providers.

> An internal user of the application should be able to ask for an exchange rate between 2 given currencies, and get back a rate that is not older than 5 minutes.
> The application should at least support 10.000 requests per day.

In practice, this should require the following 2 points:

1. Create a `live` interpreter for the `oneframe` service. This should consume the [one-frame API](https://hub.docker.com/r/paidyinc/one-frame).
2. Adapt the `rates` processes (if necessary) to make sure you cover the requirements of the use case, and work around possible limitations of the third-party provider.
3. Make sure the service's own API gets updated to reflect the changes you made in point 1 & 2.

## Getting started

Build and publish the docker image of this project.
```bash
sbt docker:publishLocal
```
Once the image is published, start all of the required images.
```bash
docker-compose up
```
Try to get the conversion rate between Indonesian Rupiah and Japanese Yen.
```bash
curl 'localhost:9090/rates?from=IDR&to=JPY'
```
What about, asking all of the possible combinations of exchange rates? Btw, technically, it's permutations.
```
curl 'localhost:9090/rates'
```

## Approach

`oneframe` service supports multiple pair of queries in one GET request. Instead of asking only one rate of AUD to USD, we can also asks other rates like GBP to USD, JPY to AUD, etc.
To get the most benefit of this, Forex will take literally every permutations of our supported currencies and caches all the rate results from `oneframe`.
This will only work if our supported currencies are minimal. Believe me, tried to query `oneframe` all of 22350 combinations of the currencies in the world in single GET, and it didn't look good.
Given that our server only supports 14 currencies, the permutation is 182 and luckily still in the acceptable range of `oneframe` server.

The main goals of forex are two:
- Overcome the limitations of 1000 invocations per day that `oneframe` server gives.
- If local cache is use, it must not older than 5 minutes.

If we call `oneframe` every 86.4 seconds, started at the very early of the day, the 1000th call will be at the very end of the day. That is, Forex tries to call `oneframe` evenly to update its cache.
Hence the cache age wouldn't be older than 86.4 seconds. And yes, we call `oneframe` greedily to update every currency combinations within each call :)

The scheduler to update the cache is implemented using fs2 [here](https://github.com/arinal/forex-mtl/blob/master/src/main/scala/forex/app/stream/updater/package.scala).

## Code practices

The initiator of this project used typelevel stacks and aimed to be more functional scala way. Aligned with this initiative, this project tries to be as much principled way
by avoiding side effects and impurity. Every impure statements will be wrapped inside a container.

Forex also structures the packages based on hexagonal architecture.

```
                               +------+
                               | boot | (boot knows everything, arrows are not shown)
                               +------+

                      +----------+   +------------+
                      | app/http |   | app/stream |
                      +---+------+   +--+---------+
                          |             |     
                          |     +-------+
                          |     |
   +--------------+  +----v-----v---+ +------------------+  +---------------+
   | interps/http |  | app/programs | | interps/inmemory |  | interps/dummy |
   +-----------+--+  +------------+-+ +--------+---------+  +-------+-------+
               |                  |            |                    | 
               |                  |            |                    | 
               |                +-v----+       |                    | 
               +--------------->| core |<------+--------------------+
                                +------+   
```
- `core` is the business logic. This layer must not depends on any infrastructure logic (kafka, cassandra, etc). Every
[aggregate root](https://dolittle.io/runtime/runtime/domain_driven_design/aggregate_root) has an algebra, which will be implemented in interpreter layer.
Since our domain is small, this is the only [algebra](https://github.com/arinal/forex-mtl/blob/master/src/main/scala/forex/core/rates/algebra.scala) `core` exposes.
Example of business logic are: generating new employee code, validating item, as long as it's doesn't require infrastructure involments.
- `interps` or interpreter and also known as infrastructure layer from DDD world. It implements the algebra exposed by `core`. e.g. `dummy` implementation generates dummy
rate which might be handy for unit testing, but `interps/http` contains a HTTP client to get the rate from another server.
- `programs` acts as an intermediary between `app` and `core` by re-exposing another algebra, but internally it calls `core`'s algebra. Please note that we shouldn't
write business logic here.
- `app` is the application layer. Forex has two `app`, one is for updating the cache every 86.4 seconds, and the other one is for serving user rate queries.
- `boot` is the topmost layer and knows everything underneath it. Since components are modularized, `boot` will wire them together and start the application.
- `commons` is everything that doesn't contains Forex specific logic, and pretty much can be reusable on other projects. Note that `commons` is not even under `forex` package.
