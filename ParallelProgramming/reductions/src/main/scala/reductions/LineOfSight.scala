package reductions

import org.scalameter._

object LineOfSightRunner {

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 40,
    Key.exec.maxWarmupRuns -> 80,
    Key.exec.benchRuns -> 100,
    Key.verbose -> true
  ) withWarmer (new Warmer.Default)

  def main(args: Array[String]) {
    val length = 10000000
    val input = (0 until length).map(_ % 100 * 1.0f).toArray
    val output = new Array[Float](length + 1)
    val seqtime = standardConfig measure {
      LineOfSight.lineOfSight(input, output)
    }
    println(s"sequential time: $seqtime ms")

    val partime = standardConfig measure {
      LineOfSight.parLineOfSight(input, output, 10000)
    }
    println(s"parallel time: $partime ms")
    println(s"speedup: ${seqtime / partime}")
  }
}

object LineOfSight {

  def max(a: Float, b: Float): Float = if (a > b) a else b

  def lineOfSight(input: Array[Float], output: Array[Float]): Unit = {

    var max = 0f
    val oValue = input.zipWithIndex.map(v =>
      if (v._2 == 0)
        0
      else {
        val c: Float = v._1 / v._2
        if (c > max) max = c

        max
      })

    Array.copy(oValue, 0, output, 0, oValue.length)
  }

  sealed abstract class Tree {
    def maxPrevious: Float
  }

  case class Node(left: Tree, right: Tree) extends Tree {
    val maxPrevious = max(left.maxPrevious, right.maxPrevious)
  }

  case class Leaf(from: Int, until: Int, maxPrevious: Float) extends Tree

  /** Traverses the specified part of the array and returns the maximum angle.
    */
  def upsweepSequential(input: Array[Float], from: Int, until: Int): Float =
    input.slice(from, until).zip(from until until).map(v => v._1 / v._2).max

  /** Traverses the part of the array starting at `from` and until `end`, and
    * returns the reduction tree for that part of the array.
    *
    * The reduction tree is a `Leaf` if the length of the specified part of the
    * array is smaller or equal to `threshold`, and a `Node` otherwise.
    * If the specified part of the array is longer than `threshold`, then the
    * work is divided and done recursively in parallel.
    */
  def upsweep(input: Array[Float], from: Int, end: Int,
              threshold: Int): Tree = {
    val len = end - from
    if (len <= threshold) Leaf(from, end, upsweepSequential(input, from, end))
    else Node(upsweep(input, from, from + len / 2, threshold), upsweep(input, from + len / 2, end, threshold))
  }

  /** Traverses the part of the `input` array starting at `from` and until
    * `until`, and computes the maximum angle for each entry of the output array,
    * given the `startingAngle`.
    */
  def downsweepSequential(input: Array[Float], output: Array[Float],
                          startingAngle: Float, from: Int, until: Int): Unit = {
    var max = startingAngle
    val oValue = input.slice(from, until).zip(from until until).map(v => v._1 / v._2).map { v =>
      if (v > max) max = v

      max
    }

    Array.copy(oValue, 0, output, from, oValue.length)
  }

  /** Pushes the maximum angle in the prefix of the array to each leaf of the
    * reduction `tree` in parallel, and then calls `downsweepTraverse` to write
    * the `output` angles.
    */
  def downsweep(input: Array[Float], output: Array[Float], startingAngle: Float,
                tree: Tree): Unit = {

    def findLeaf(t: Tree): List[Leaf] = {
      tree match {
        case n: Node => findLeaf(n.left) ++ findLeaf(n.right)
        case n: Leaf => n :: Nil
      }
    }

    var max = startingAngle

    findLeaf(tree).foreach(v => {
      val vv = upsweepSequential(input, v.from, v.until)
      if (vv > max) max = vv
      downsweepSequential(input, output, max, v.from, v.until)
    })
  }

  /** Compute the line-of-sight in parallel. */
  def parLineOfSight(input: Array[Float], output: Array[Float],
                     threshold: Int): Unit = {
    val tree = upsweep(input.sorted, 0, input.length, threshold)
    downsweep(input, output, 0, tree)
  }
}
