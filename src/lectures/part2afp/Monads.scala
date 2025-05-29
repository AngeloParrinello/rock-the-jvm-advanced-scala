package lectures.part2afp

object Monads extends App {

  // our own Try monad

  trait Attempt[+A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B]
  }

  object Attempt {
    // we need to use a call by name parameter to avoid the evaluation of the parameter
    // we don't want to evaluate the parameter until we actually use it because can throw an exception
    // unit or pure or apply method
    def apply[A](a: => A): Attempt[A] =
    try {
      Success(a)
    } catch {
      case e: Throwable => Fail(e)
    }
  }

  case class Success[A](value: A) extends Attempt[A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B] = try {
      f(value)
    } catch {
      case e: Throwable => Fail(e)
    }
  }

  case class Fail(e: Throwable) extends Attempt[Nothing] {
    def flatMap[B](f: Nothing => Attempt[B]): Attempt[B] = this // we cannot flatMap a Fail, we'll just return the same failure
  }

  /*
  * left-identity
  * unit.flatMap(f) = f(x)
  *
  * Attempt(x).flatMap(f) = f(x) // This only make sense for the success case
  * Success(x).flatMap(f) = f(x) // proved
  *
  * right-identity
  * attempt.flatMap(unit) = attempt
  *
  * Success(x).flatMap(x => Attempt(x)) = Attempt(x) = Success(x)
  * Failure(e).flatMap(...) = Failure(e) // because the flatMap method of Failure returns itself
  *
  * associativity
  * attempt.flatMap(f).flatMap(g) == attempt.flatMap(x => f(x).flatMap(g))
  *
  * Fail(e).flatMap(f).flatMap(g) = Fail(e)
  * Fail(e).flatMap(x => f(x).flatMap(g)) = Fail(e) // Fail satisfies the associativity law
  *
  * Success(v).flatMap(f).flatMap(g) = f(v).flatMap(g) OR Fail(e)
  * Success(v).flatMap(x => f(x).flatMap(g)) = f(v).flatMap(g) OR Fail(e) // Success satisfies the associativity law
  *
   */

  val attempt = Attempt {
    throw new RuntimeException("My own monad, yes!")
  }

  println(attempt)

  /*
  * Exercise:
  *
  * 1) implement a Lazy[T] monad = computation which will only be executed when it's needed.
  *
  * unit / apply / pure for a Lazy[T] companion object
  * flatMap
  *
  * 2) Monads = unit + flatMap
  *  Monads = unit + map + flatten  // alternative definition
  * How to transform a monad with flatMap into a monad with map and flatten?
  *
  * Monad[T] {
  *  def flatMap[B](f: T => Monad[B]): Monad[B] = ... (implemented)
  * def map[B](f: T => B): Monad[B] = ???
  * def flatten(m: Monad[Monad[T]]): Monad[T] = ???
  *
  * (have List in mind)
  * List(1,2,3).map(_ * 2) =List(1,2,3).flatMpa(x => List(x * 2))= List(2,4,6)
  * List(List(1,2), List(3,4)).flatten = List(List(1,2), List(3,4)).flatMap(x => x) = List(1,2,3,4)
   */

}
