package lectures.part4implicits

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

object MagnetPattern extends App {
  // used to resolve some problems with method overloading
  // the magnet pattern is a way to solve the problem of method overloading with different types

  class P2PRequest
  class P2PResponse
  class Serializer[T]

  trait Actor {
    def receive(statusCode: Int): Int
    def receive(request: P2PRequest): Int
    def receive(response: P2PResponse): Int
    def receive[T : Serializer](message: T): Int
    def receive[T : Serializer](message: T, statusCode: Int): Int
    def receive(future: Future[P2PRequest]): Int
    //... and more lots of overloads
    // and this blocks us to create other methods such as
    // def receive(future: Future[P2PResponse]): Int
    // this one does not compile because it has the same type erasure as the previous one -->
    // at the compile time these two methods take a Future
  }

  /*
  Problems:
    1 - type erasure
    2 - lifting doesn't work for all overloads

    val receiveFV = receive _ // ?! does not work ... what is the type of _?

    3 - code duplication
    4 - type inference and default args
    actor.receive( ) --> what is the default argument?
   */

  trait MessageMagnet[Result] {
    def apply(): Result
  }

  def receive[R](magnet: MessageMagnet[R]): R = magnet()

  implicit class FromP2PRequest(request: P2PRequest) extends MessageMagnet[Int] {
    def apply(): Int = {
      // logic for handling P2PRequest
      println("Handling P2PRequest")
      42
    }
  }

  implicit class FromP2PResponse(response: P2PResponse) extends MessageMagnet[Int] {
    def apply(): Int = {
      // logic for handling P2PResponse
      println("Handling P2PResponse")
      24
    }
  }

  receive(new P2PRequest)
  receive(new P2PResponse)

  // 1 - no more type erasure problems!
  // now I can define :
  implicit class FromResponseFuture(future: Future[P2PResponse]) extends MessageMagnet[Int] {
    override def apply(): Int = {
      // logic for handling P2PResponse
      println("Handling P2PResponse future")
      24
    }
  }

  implicit class FromRequestFuture(future: Future[P2PRequest]) extends MessageMagnet[Int] {
    override def apply(): Int = {
      // logic for handling P2PRequest
      println("Handling P2PRequest future")
      42
    }
  }

  receive(Future(new P2PRequest))
  receive(Future(new P2PResponse))
  // this one works

  // 2 - lifting works
  trait MathLib {
    def add1(x: Int): Int = x + 1
    def add1(s: String): Int = s.toInt + 1
    // add1 overloads
  }

  // if we want to magnetize this, we can do it like this
  trait AddMagnet {
    def apply(): Int
  }

  def add1(magnet: AddMagnet): Int = magnet()

  implicit class AddInt(x: Int) extends AddMagnet {
    override def apply(): Int = x + 1
  }

  implicit class AddString(s: String) extends AddMagnet {
    override def apply(): Int = s.toInt + 1
  }

  val addFV = add1 _ // this works
  println(addFV(1))
  println(addFV("3"))
  // now we can lift our magnetized methods for use in higher order functions

  // whereas we cannot do..
  // val receiveFV = receive _ // ?! does not work ... what is the type of _?
  // receiveFV(new P2PResponse) // this does not work

  /*
  Drawbacks
  1 - SUPER verbose
  2 - harder to read and comprehend
  3 - you can't name or place default arguments
  you cannot do this:
 receive() // what is the default argument?
  4 - call by name doesn't work correctly
   */

  class Handler {
    def handle(s: => String) = {
      println(s)
      println(s)
    }
    // other overloads
  }

  trait HandleMagnet {
    def apply(): Unit
  }

  def handle(magnet: HandleMagnet): Unit = magnet()

  implicit class StringHandle(s: => String) extends HandleMagnet {
    override def apply(): Unit = {
      println(s)
      println(s)
    }
  }

  def sideEffectMethod: String = {
    println("Hello, Scala")
    "magnet"
  }

  handle(sideEffectMethod)
  // and this returns:
  // Hello, Scala
  // magnet
  // twice

  // but if I write
  handle {
    println("Hello, Scala")
    "magnet"
  }

  // we will see
  // Hello, Scala
  // magnet
  // magnet
  // because the side effect (the println) is evaluated only once
  // this is like writing:
  handle{
    println("Hello, Scala")
    new StringHandle("magnet")
  }

  // super hard to trace and debug!!


}
