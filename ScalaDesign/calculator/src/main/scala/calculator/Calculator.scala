package calculator

sealed abstract class Expr

final case class Literal(v: Double) extends Expr

final case class Ref(name: String) extends Expr

final case class Plus(a: Expr, b: Expr) extends Expr

final case class Minus(a: Expr, b: Expr) extends Expr

final case class Times(a: Expr, b: Expr) extends Expr

final case class Divide(a: Expr, b: Expr) extends Expr

object Calculator {
  def computeValues(namedExpressions: Map[String, Signal[Expr]]):
  Map[String, Signal[Double]] = {
    namedExpressions.map { n =>
      n._1 -> Signal(
        if (detectLoop(n._1, namedExpressions)) {
          println(s">>> loop: ${n._1}")
          Double.NaN
        }
        else {
          println(s">>> no loop: ${n._1}")
          eval(n._2(), namedExpressions)
        }
      )
    }
  }

  def detectLoop(name: String, references: Map[String, Signal[Expr]]): Boolean = {
    println(s">>> detectLoopRef : ${name}")
    def loopDetect(nameList: Set[String], expr: Expr): Boolean = expr match {
      case Ref(nameRef: String) =>
        if (nameList.contains(nameRef)) true
        else
          loopDetect(nameList + nameRef, getReferenceExpr(nameRef, references))

      case Plus(a: Expr, b: Expr) => loopDetect(nameList, a) || loopDetect(nameList, b)
      case Minus(a: Expr, b: Expr) => loopDetect(nameList, a) || loopDetect(nameList, b)
      case Times(a: Expr, b: Expr) => loopDetect(nameList, a) || loopDetect(nameList, b)
      case Divide(a: Expr, b: Expr) => loopDetect(nameList, a) || loopDetect(nameList, b)
      case _ => false
    }

    loopDetect(Set(name), getReferenceExpr(name, references))
  }

  def eval(expr: Expr, references: Map[String, Signal[Expr]]): Double = {
    expr match {
      case Literal(v: Double) => v
      case Ref(name: String) =>
          eval(getReferenceExpr(name, references), references)

      case Plus(a: Expr, b: Expr) => eval(a, references) + eval(b, references)
      case Minus(a: Expr, b: Expr) => eval(a, references) - eval(b, references)
      case Times(a: Expr, b: Expr) => eval(a, references) * eval(b, references)
      case Divide(a: Expr, b: Expr) => eval(a, references) / eval(b, references)
    }
  }

  /** Get the Expr for a referenced variables.
    * If the variable is not known, returns a literal NaN.
    */
  private def getReferenceExpr(name: String,
                               references: Map[String, Signal[Expr]]) = {
    references.get(name).fold[Expr] {
      Literal(Double.NaN)
    } { exprSignal =>
      exprSignal()
    }
  }
}
