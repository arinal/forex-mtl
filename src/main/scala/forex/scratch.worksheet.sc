val numbers = List(1, 2, 3)

def permute(set: Set[Int]): Set[(Int, Int)] = {
  for {
    n     <- set
    shadow = set - n
    n1    <- shadow
  } yield (n, n1)
}
