package reductions

import org.scalameter._

object ParallelParenthesesBalancingRunner {

  @volatile var seqResult = false

  @volatile var parResult = false

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 40,
    Key.exec.maxWarmupRuns -> 80,
    Key.exec.benchRuns -> 120,
    Key.verbose -> true
  ) withWarmer (new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val length = 100000000
    val chars = new Array[Char](length)
    val threshold = 10000
    val seqtime = standardConfig measure {
      seqResult = ParallelParenthesesBalancing.balance(chars)
    }
    println(s"sequential result = $seqResult")
    println(s"sequential balancing time: $seqtime ms")

    val fjtime = standardConfig measure {
      parResult = ParallelParenthesesBalancing.parBalance(chars, threshold)
    }
    println(s"parallel result = $parResult")
    println(s"parallel balancing time: $fjtime ms")
    println(s"speedup: ${seqtime / fjtime}")
  }
}

object ParallelParenthesesBalancing {

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
    */
  def balance(chars: Array[Char]): Boolean = {
    val vLeft = '('
    val vRight = ')'
    val parenthesesArray: List[Char] = chars.filter(c => c == vLeft || c == vRight).toList

    // 0 null -1 left 1 right
    def find(ps: List[Char], stack: Int): Boolean = {
      ps match {
        case Nil => if (stack == 0) true else false
        case x :: tail => x match {
          case '(' =>
            find(tail, stack + 1)
          case ')' => if (stack > 0) find(tail, stack - 1) else false
        }
      }
    }

    find(parenthesesArray, 0)
  }

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
    */
  def parBalance(chars: Array[Char], threshold: Int): Boolean = {

    val vLeft = '('
    val vRight = ')'
    val parenthesesArray: List[Char] = chars.filter(c => c == vLeft || c == vRight).toList

    // idx: begin util: end arg1: chartsLength, arg2: threshold
    def traverse(idx: Int, until: Int, arg1: Int, arg2: Int): List[List[Char]] = {
      parenthesesArray.slice(idx, until).sliding(arg1, arg2).toList
    }

    def reduce(from: Int, until: Int): Int = {
      def find(ps: List[Char], stack: Int): Int = {
        ps match {
          case Nil => stack
          case x :: tail => x match {
            case '(' =>
              find(tail, stack + 1)
            case ')' => find(tail, stack - 1)
          }
        }
      }
      traverse(from, until, threshold, threshold).map(v => find(v, 0)).sum
    }

    reduce(0, parenthesesArray.length) == 0
  }

  // For those who want more:
  // Prove that your reduction operator is associative!

}
