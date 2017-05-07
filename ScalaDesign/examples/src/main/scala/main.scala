package Learning.Examples

object Median {
  def apply[@specialized(Int, Double, Long, Float) T](f: T): T = f match {
    case v: Int => (v + v).asInstanceOf[T]
    case v: Double => (v * 3).asInstanceOf[T]
  }
}

object Main extends App {
  println("Hello World!")

  println(Median(2))

  //(0 to 20).Median
}