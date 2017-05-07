package reductions

import org.scalameter._

object ParallelCountChangeRunner {

  @volatile var seqResult = 0

  @volatile var parResult = 0

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 20,
    Key.exec.maxWarmupRuns -> 40,
    Key.exec.benchRuns -> 80,
    Key.verbose -> true
  ) withWarmer (new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val amount = 250
    val coins = List(1, 2, 5, 10, 20, 50)
    val seqtime = standardConfig measure {
      seqResult = ParallelCountChange.countChange(amount, coins)
    }
    println(s"sequential result = $seqResult")
    println(s"sequential count time: $seqtime ms")

    def measureParallelCountChange(threshold: ParallelCountChange.Threshold): Unit = {
      val fjtime = standardConfig measure {
        parResult = ParallelCountChange.parCountChange(amount, coins, threshold)
      }
      println(s"parallel result = $parResult")
      println(s"parallel count time: $fjtime ms")
      println(s"speedup: ${seqtime / fjtime}")
    }

    measureParallelCountChange(ParallelCountChange.moneyThreshold(amount))
    measureParallelCountChange(ParallelCountChange.totalCoinsThreshold(coins.length))
    measureParallelCountChange(ParallelCountChange.combinedThreshold(amount, coins))
  }
}

object ParallelCountChange {

  /** Returns the number of ways change can be made from the specified list of
    * coins for the specified amount of money.
    */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else {

      def cal(money: Int, coins: List[Int], path: List[Int]): Int =
        coins.map { x => {
          val total = (path :+ x).sum

          if (total < money) cal(money, coins.dropWhile(_ < x), path :+ x)
          else if (total == money) {
            //            println((path :+ x).mkString("[", ", ", "]"))
            1
          }
          else 0
        }
        }.sum

      cal(money, coins.sorted, List[Int]())
    }
  }

  type Threshold = (Int, List[Int]) => Boolean

  /** In parallel, counts the number of ways change can be made from the
    * specified list of coins for the specified amount of money.
    */
  def parCountChange(money: Int, coins: List[Int], threshold: Threshold): Int = {
    if (money == 0) 1
    else {

      def cal(m: Int, c: List[Int], path: List[Int]): Int =
        c.map { x => {
          val total = (path :+ x).sum

          if (total < m) cal(m, c.dropWhile(_ < x), path :+ x)
          else if (total == m) {
            //            println((path :+ x).mkString("[", ", ", "]"))
            1
          }
          else 0
        }
        }.sum

      def parCal(m: Int, c: List[Int], path: List[Int]): Int =
        c.par.map { x => {
          val total = (path :+ x).sum

          if (total < m) cal(m, c.dropWhile(_ < x), path :+ x)
          else if (total == m) {
            //            println((path :+ x).mkString("[", ", ", "]"))
            1
          }
          else 0
        }
        }.sum

      if (threshold(money, coins))
        cal(money, coins.sorted, List[Int]())
      else parCal(money, coins.sorted, List[Int]())
    }
  }

  /** Threshold heuristic based on the starting money. */
  def moneyThreshold(startingMoney: Int): Threshold =
    (realValue: Int, _: List[Int]) => if (realValue < 2 * startingMoney / 3) true else false

  /** Threshold heuristic based on the total number of initial coins. */
  def totalCoinsThreshold(totalCoins: Int): Threshold =
    (_: Int, currentCoins: List[Int]) => if (currentCoins.length < 2 * totalCoins / 3) true else false


  /** Threshold heuristic based on the starting money and the initial list of coins. */
  def combinedThreshold(startingMoney: Int, allCoins: List[Int]): Threshold = {
    (realValue: Int, currentCoins: List[Int]) =>
      if (realValue * currentCoins.length <= (startingMoney * allCoins.length / 2)) true else false
  }
}
