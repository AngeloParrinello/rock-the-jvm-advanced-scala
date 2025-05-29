package lectures.part2afp

object PartialFunctions extends App {

  val aFunction = (x: Int) => x + 1 // Function1[Int, Int] === Int => Int // aFunction is a total function
  // this means that aFunction is defined for all values of x (Int)
  // but sometimes we want to define a function only for a subset of values of x
  // this is called a partial function
  // a partial function is a function that is defined only for a subset of values of the input type
  val aFussyFunction = (x: Int) =>
    if (x == 1) 42
    else if (x == 2) 56
    else if (x == 5) 999
    else throw new FunctionNotApplicableException

  class FunctionNotApplicableException extends RuntimeException

  // quite clunky implementation
  // let's use the pattern matching syntax
  val aNicerFussyFunction = (x: Int) => x match {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  }
  // this function is a function that is defined only for the values 1, 2, 5
  // so {1, 2, 5} => Int
  // and there is a trait in Scala that represents this kind of function

  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  } // partial function value

  println(aPartialFunction(2))
  // println(aPartialFunction(57273)) // this will throw a MatchError, because partial functions are based on pattern matching

// partial functions can be lifted to total functions returning an Option
  // PF utilities
  println(aPartialFunction.isDefinedAt(67)) // false

  // lift
  // basically, it returns a total function Int => Option[Int]
  // Some(56) if the partial function is defined at 2
  // None if the partial function is not defined at 98
  val lifted = aPartialFunction.lift // Int => Option[Int]
  println(lifted(2)) // Some(56)
  println(lifted(98)) // None

  val pfChain = aPartialFunction.orElse[Int, Int] {
    case 45 => 67
  }

  println(pfChain(5)) // 999
  println(pfChain(45)) // 67

  // PF extends normal functions
  val aTotalFunction: Int => Int = {
    case 1 => 99
  }

  // HOFs accept partial functions as well
  val aMappedList = List(1, 2, 3).map {
    case 1 => 42
    case 2 => 56
    case 3 => 999
  }

  println(aMappedList)

  /*
    Note: Unlike normal functions, PF can only have ONE parameter type
   */

  // Exercise
  /*
    1 - construct a PF instance yourself (by instantiate a PartialFunction anonymous class)
    2 - dumb chat bot as a PF
   */

  // 1

  val aManualFussyFunction = new PartialFunction[Int, Int] {
    override def isDefinedAt(x: Int): Boolean = x == 1 || x == 2 || x == 5

    override def apply(v1: Int): Int = v1 match {
      case 1 => 42
      case 2 => 56
      case 5 => 999
    }
  }

  println(aManualFussyFunction(2))
  println(aManualFussyFunction.isDefinedAt(67))

  // 2

  // initial one:
  // scala.io.Source.stdin.getLines().foreach(line => println("you said: " + line))

  // do that thing as a partial function
  val chatbot: PartialFunction[String, String] = {
    case "hello" => "Hi, my name is HAL 9000"
    case "goodbye" => "Once you start talking to me, there is no turning back"
    case "call mom" => "Unable to find your phone without your credit card"
  }

  // scala.io.Source.stdin.getLines().foreach(line => println("you said: " + chatbot(line)))
  scala.io.Source.stdin.getLines().map(chatbot).foreach(println) // this will print the responses of the chatbot

  /*
  Takeaways:
  - Partial functions are based on pattern matching
  - Partial functions can only have one parameter type
  - Utilities for partial functions: isDefinedAt, lift, orElse
  - Used in practice with collections with map, collect, orElse, etc.
   */

  /*
  Functional Seq:
  - Sequences are "CALLABLE" through an integer index (apply method)
  - So they are basically PARTIALLY FUNCTIONS DEFINED IN THE DOMAIN [0, length - 1]
  - Maps are partial functions too, defined on the domain of the keys
   */
}
