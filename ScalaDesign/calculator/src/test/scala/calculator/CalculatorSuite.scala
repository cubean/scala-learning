package calculator

import calculator.TweetLength.MaxTweetLength
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, _}

@RunWith(classOf[JUnitRunner])
class CalculatorSuite extends FunSuite with ShouldMatchers {

  /** ****************
    * * TWEET LENGTH **
    * *****************/

  def tweetLength(text: String): Int =
    text.codePointCount(0, text.length)

  test("tweetRemainingCharsCount with a constant signal") {
    val result = TweetLength.tweetRemainingCharsCount(Var("hello world"))
    assert(result() == MaxTweetLength - tweetLength("hello world"))

    val tooLong = "foo" * 200
    val result2 = TweetLength.tweetRemainingCharsCount(Var(tooLong))
    assert(result2() == MaxTweetLength - tweetLength(tooLong))
  }

  test("tweetRemainingCharsCount with a supplementary char") {
    val result = TweetLength.tweetRemainingCharsCount(Var("foo blabla \uD83D\uDCA9 bar"))
    assert(result() == MaxTweetLength - tweetLength("foo blabla \uD83D\uDCA9 bar"))
  }


  test("colorForRemainingCharsCount with a constant signal") {
    val resultGreen1 = TweetLength.colorForRemainingCharsCount(Var(52))
    assert(resultGreen1() == "green")
    val resultGreen2 = TweetLength.colorForRemainingCharsCount(Var(15))
    assert(resultGreen2() == "green")

    val resultOrange1 = TweetLength.colorForRemainingCharsCount(Var(12))
    assert(resultOrange1() == "orange")
    val resultOrange2 = TweetLength.colorForRemainingCharsCount(Var(0))
    assert(resultOrange2() == "orange")

    val resultRed1 = TweetLength.colorForRemainingCharsCount(Var(-1))
    assert(resultRed1() == "red")
    val resultRed2 = TweetLength.colorForRemainingCharsCount(Var(-5))
    assert(resultRed2() == "red")
  }

  test("Polynomial - test 1, 4, 4") {
    val delta1 = Polynomial.computeDelta(Signal(1.0), Signal(4.0), Signal(4.0))
    assert(delta1() == 0)

    val polyResult = Polynomial.computeSolutions(Signal(1.0), Signal(4.0), Signal(4.0), delta1)
    assert(polyResult() == Set(-2.0))
  }

  test("Polynomial - test 0, 4, 4") {
    val delta1 = Polynomial.computeDelta(Signal(0), Signal(4.0), Signal(4.0))
    assert(delta1() == 16)

    val polyResult = Polynomial.computeSolutions(Signal(0), Signal(4.0), Signal(4.0), delta1)
    assert(polyResult() == Set(-1.0))
  }

  test("Polynomial - test 0, 0, 4") {
    val delta1 = Polynomial.computeDelta(Signal(0), Signal(0.0), Signal(4.0))
    assert(delta1() == 0)

    val polyResult = Polynomial.computeSolutions(Signal(0), Signal(0.0), Signal(4.0), delta1)
    assert(polyResult() == Set())
  }

  test("calculator - computeValues ") {
    val vv = Calculator.computeValues(
      Map(
        "a" -> Signal(Literal(30)),
        "b" -> Signal(Plus(Literal(30), Ref("a"))),
        "c" -> Signal(Times(Ref("b"), Ref("a"))),
        "d" -> Signal(Divide(Ref("c"), Literal(30))),
        "e" -> Signal(Times(Ref("b"), Ref("f"))),
        "f" -> Signal(Times(Ref("g"), Ref("a"))),
        "g" -> Signal(Minus(Ref("c"), Ref("e")))
      )
    )

    assert(vv("a")() == 30)
    assert(vv("b")() == 60)
    assert(vv("c")() == 1800)
    assert(vv("d")() == 60)
    assert(vv("e")().isNaN)
    assert(vv("f")().isNaN)
    assert(vv("g")().isNaN)
  }
}
