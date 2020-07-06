# A local proxy for Forex rates

Build a local proxy for getting Currency Exchange Rates

__Requirements__

[Forex](forex-mtl) is a simple application that acts as a local proxy for getting exchange rates.
It's a service that can be consumed by other internal services to get the exchange rate between a set of currencies,
so they don't have to care about the specifics of third-party providers.

> An internal user of the application should be able to ask for an exchange rate between 2 given currencies, and get back a rate that is not older than 5 minutes.
> The application should at least support 10.000 requests per day.

In practice, this should require the following 2 points:

1. Create a `live` interpreter for the `oneframe` service. This should consume the [one-frame API](https://hub.docker.com/r/paidyinc/one-frame).
2. Adapt the `rates` processes (if necessary) to make sure you cover the requirements of the use case, and work around possible limitations of the third-party provider.
3. Make sure the service's own API gets updated to reflect the changes you made in point 1 & 2.

## Approach

`oneframe` service supports multiple pair of queries in one GET request. Instead of asking only one rate of AUD to USD, we can also asks other rates like GBP to USD, JPY to AUD, etc.
To get the most benefit from this, our service will take literally every permutations of our supported currencies and caches all the rate results from `oneframe`.
This will only work if our supported currencies are minimal. Tried to query `oneframe` all of 22350 combinations of the currencies in the world in single GET, and it didn't look good.
Given that our server only supports 

