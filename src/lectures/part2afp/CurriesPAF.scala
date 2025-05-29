package lectures.part2afp

object CurriesPAF extends App {

    // curried functions
    val superAdder: Int => Int => Int =
      x => y => x + y

    // we can define the function above as
    val add3 = superAdder(3) // y => 3 + y
    println(add3(5))
    println(superAdder(3)(5)) // curried function

    // METHOD!
    // same as superAdder, but with syntax sugar
    def curriedAdder(x: Int)(y: Int): Int = x + y // curried method

    // lifting = ETA-EXPANSION
    // if I remove the signature of the method (Int => Int), the compiler will complain
    // saying "missing argument list for method curriedAdder"
    // we are attempting to call this method with fewer arguments than it expects
  // am I defining a function or getting a result from a curried function?
    val add4: Int => Int = curriedAdder(4)
   // under the scenes, the compiler is doing the following: LIFTING or ETA-EXPANSION
    // functions != methods (due to the JVM limitation)
  // the original problem is that we defined the curriedAdder as a method (def)
  // and we are trying to use it as a function (val). And we want to use the function in an higher order function
  // we can't use methods in higher order functions, unless they are transformed into functions
  // that's a limitation of the JVM because methods are part of instances of classes (or in this case
  // the singleton object CurriesPAF) and functions are instances of classes that extend Function1, Function2, etc.
  // so transforming a method into a function is called LIFTING or ETA-EXPANSION
  // the compiler does this automatically for us
  // the compiler will transform the method into a function
  def inc(x: Int): Int = x + 1
  List(1, 2, 3).map(inc) // ETA-expansion for us and it turns inc into a function in x => inc(x)

    // Partial function applications
    val add5 = curriedAdder(5) _ // Int => Int
  // with the underscore, we are telling the compiler to transform the method into a function
  // we are forcing the compiler to do the ETA-expansion
  // and we are saying to the compiler to transform the method into a function after you applied the first argument (curriedAdder(5))
  // and if you hover to add5, you will see that it's a function

    // Exercise
    val simpleAddFunction = (x: Int, y: Int) => x + y
    def simpleAddMethod(x: Int, y: Int): Int = x + y
    def curriedAddMethod(x: Int)(y: Int): Int = x + y

    // define a add7 function with this signature Int => Int = y => 7 + y

    // add7: Int => Int = y => 7 + y
    // as many different implementations of add7 using the above
    val add7 = (x: Int) => simpleAddFunction(7, x)
    val add7_2 = simpleAddFunction.curried(7) // convert a normal function into a curried function
    val add7_6 = simpleAddFunction(7, _: Int) // and this works as well
    val add7_3 = curriedAddMethod(7) _ // PAF
    val add7_4 = simpleAddMethod(7, _: Int) // alternative syntax for turning methods into function values
    val add7_5 = curriedAddMethod(7)(_) // PAF - alternative syntax
    val add7_7 = simpleAddFunction(7, _)

  // underscores are powerful in Scala
  def concatenator(a: String, b: String, c: String): String = a + b + c
  val insertName = concatenator("Hello, I'm ", _: String, ", how are you?") // x => concatenator("Hello, I'm ", x, ", how are you?")
  println(insertName) // lectures.part2afp.CurriesPAF$$$Lambda$19/13648335@129a8472
  println(insertName("Daniel")) // Hello, I'm Daniel, how are you?

  val fillInTheBlanks = concatenator("Hello, ", _: String, _: String) // (x, y) => concatenator("Hello, ", x, y)
  println(fillInTheBlanks("Daniel", " Scala is awesome!")) // Hello, Daniel Scala is awesome!

  // Exercises
  /*
    1. Process a list of numbers and return their string representations with different formats
       Use the %4.2f, %8.6f and %14.12f with a curried formatter function.
   */

  /*
    2. difference between
       - functions vs methods
       - parameters: by-name vs 0-lambda
   */
  def byName(n: => Int) = n + 1
  def byFunction(f: () => Int) = f() + 1

  def method: Int = 42
  def parenMethod(): Int = 42

  /*
    calling byName and byFunction
    - int
    - method
    - parenMethod
    - lambda
    - PAF (partially applied function)
   */
  //  Which cases compile and which don't?


  // Ex. 1
  def curriedFormatter(s: String)(number: Double): String = s.format(number)
  val numbers = List(Math.PI, Math.E, 1, 9.8, 1.3e-12)
  val simpleFormat = curriedFormatter("%4.2f") _ // lift
  val seriousFormat = curriedFormatter("%8.6f") _ // lift
  val preciseFormat = curriedFormatter("%14.12f") _ // lift
  println(numbers.map(simpleFormat))
  println(numbers.map(seriousFormat))
  println(numbers.map(preciseFormat))

  println(numbers.map(curriedFormatter("%14.12f"))) // compiler does sweet eta-expansion for us, without the underscore because since it
  // is passed to an higher order function, the compiler will do the ETA-expansion for us and extract the value from the function

  // Ex. 2
  // byName
  byName(23)
  byName(method)
  byName(parenMethod())
  byName(parenMethod) // the compiler does ETA-expansion, ok but beware ==> byName(parenMethod()). By name is not the same as a 0-arity function! The first one
  // is a by-name parameter, the second one is a 0-arity function. So the first one expects a value, the second one expects a function that returns a value
  // byName(() => 42) // not ok
  byName((() => 42)()) // ok
  // byName(parenMethod _) // not ok

  // byFunction
  // byFunction(23) // not ok
   //byFunction(method) // not ok!!!! does not do ETA-expansion
  byFunction(parenMethod) // compiler does ETA-expansion
  byFunction(method _) // also works, but unnecessary
  byFunction(() => 46) // works
  byFunction(parenMethod _) // also works, but unnecessary, because the compiler does ETA-expansion for us without the underscore

}
