package lectures.part5ts

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object HigherKindedTypes extends App {
  // deeper generics types with some unknown type parameter at the deepest level

  trait HigherKindedType[F[_]] // this is a type constructor, it takes a type parameter
  // what are they use for?

  trait MyList[T] {
    def flatMap[B](f: T => B): MyList[B]
  }

  trait MyOption[T] {
    def flatMap[B](f: T => B): MyOption[B]
  }

  trait MyFuture[T] {
    def flatMap[B](f: T => B): MyFuture[B]
  }

  // and son and so forth

  // combine/multiply List(1,2) x List("a", "b") => List(1a, 1b, 2a, 2b)

//  def multiply[A, B](listA: List[A], listB: List[B]): List[(A, B)] =
//    for {
//      a <- listA
//      b <- listB
//    } yield (a, b)
//
//  def multiply[A, B](optionA: Option[A], optionB: Option[B]): Option[(A, B)] =
//    for {
//      a <- optionA
//      b <- optionB
//    } yield (a, b)
//
//  def multiply[A, B](futureA: Future[A], futureB: Future[B]): Future[(A, B)] =
//    for {
//      a <- futureA
//      b <- futureB
//    } yield (a, b)

  // but the code is almost the same, so we can abstract it
  // even though the concept is the same, the types are different
  // they are completely unrelated concepts but with the same implementation

  // HIGHER KIND TYPES TO THE RESCUE
  // F[_] can be either Future, Option, List, etc.
  // and the type parameter (A), it's that the type that monad (Future, Option, List, etc.) wraps
  // i.e. F[_] is a Future and A is an Int
  trait Monad[F[_], A] { // this is a type constructor
    // as we know from the monad lecture the fundamental operation in the monadic structure is flatMap
    def flatMap[B](f: A => F[B]): F[B]
    def map[B](f: A => B): F[B]
  }

  class MonadListInt(list: List[Int]) extends Monad[List, Int] {
    // the compiler automatically knows that the type parameter A is Int and so the flatmap method will take
    // a function from Int to List[B]
    override def flatMap[B](f: Int => List[B]): List[B] = list.flatMap(f)
    override def map[B](f: Int => B): List[B] = list.map(f)
  }

  // but the definition above accepts only Int, let's generalize it...
  class MonadList[A](list: List[A]) extends Monad[List, A] {
    // the compiler automatically knows that the type parameter A is Int and so the flatmap method will take
    // a function from Int to List[B]
    override def flatMap[B](f: A => List[B]): List[B] = list.flatMap(f)
    override def map[B](f: A => B): List[B] = list.map(f)
  }

  val monadList = new MonadList(List(1, 2, 3))
  monadList.flatMap(x => List(x, x + 1)) // List[Int]
  // Monad[List, Int] => List[Int]
  monadList.map(_ * 2) // List[Int]
  // Monad[List, Int] => List[Int]

  // but why do we do this work?
  // because we want to abstract the concept of a monad and we want to write generic code that works for all monads
  // and so we can say that, in general, the definition of multiply is:
  // the type parameter A and B which are the types that the monad wraps and so the types that we are going to multiple/combine i.e. String and Int
  // and the structure that wraps them, the type of the monad itself, i.e. List, Option, Future, etc.
  def multiply[F[_], A, B](ma: Monad[F, A], mb: Monad[F, B]): F[(A, B)] = {
    for {
      a <- ma
      b <- mb
    } yield (a, b)
    // ma.flatMap(a => mb.map(b => (a, b)))
  }

  // and so we can write the following
  println(multiply(new MonadList(List(1,2)), new MonadList(List("a", "b"))))

  // and now we can reuse the code, for example, we can write a MonadOption
  class MonadOption[A](option: Option[A]) extends Monad[Option, A] {
    override def flatMap[B](f: A => Option[B]): Option[B] = option.flatMap(f)
    override def map[B](f: A => B): Option[B] = option.map(f)
  }

  println(multiply(new MonadOption(Some(2)), new MonadOption(Some("scala"))))

  // so we can apply multiply to any structure that has a Monad implementation (i.e. has a flatMap method)!!! Amazing!!!

  // wouldn't be nice to convert automatically the monad wrapper to the actual monad?
  // how do we do that? with implicit conversions
  // so let's define implicit the classes above...

  implicit class MonadListImplicit[A](list: List[A]) extends Monad[List, A] {
    override def flatMap[B](f: A => List[B]): List[B] = list.flatMap(f)
    override def map[B](f: A => B): List[B] = list.map(f)
  }

  implicit class MonadOptionImplicit[A](option: Option[A]) extends Monad[Option, A] {
    override def flatMap[B](f: A => Option[B]): Option[B] = option.flatMap(f)
    override def map[B](f: A => B): Option[B] = option.map(f)
  }


  def multiply[F[_], A, B](implicit ma: Monad[F, A], mb: Monad[F, B]): F[(A, B)] = {
    for {
      a <- ma
      b <- mb
    } yield (a, b)
    // ma.flatMap(a => mb.map(b => (a, b)))
  }

  // and since now we have defined the implicit classes above, we can write the following
  println(multiply(List(1,2), List("a", "b"))) // because now the compiler looks for the implicit conversion
  // and will rewrite the code as multiply(new MonadList(List(1,2)), new MonadList(List("a", "b")))
  println(multiply(Some(2), Some("scala"))) // because now the compiler looks for the implicit conversion

}
