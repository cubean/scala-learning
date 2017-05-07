package calculator

object Polynomial {
  def computeDelta(a: Signal[Double], b: Signal[Double],
                   c: Signal[Double]): Signal[Double] = {

    Signal(b() * b() - 4 * a() * c())
  }

  implicit class Piper[A](private val a: A) extends AnyVal {
    def |>[B](f: A => B) = f(a)
  }

  def computeSolutions(a: Signal[Double], b: Signal[Double],
                       c: Signal[Double], delta: Signal[Double]): Signal[Set[Double]] = {
    Signal(
      if (a() == 0)
        if (b() == 0)
          Set.empty
        else
          Set[Double](-c() / b())
      else {
        if (delta() >= 0)
          Math.pow(delta(), 0.5) |> {
            d => Set[Double]((-b() + d) / (2 * a()), (-b() - d) / (2 * a()))
          }
        else Set.empty
      }
    )
  }
}
