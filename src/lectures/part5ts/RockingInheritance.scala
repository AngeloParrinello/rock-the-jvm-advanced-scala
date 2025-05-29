package lectures.part5ts

object RockingInheritance extends App {

  // convenience
  // I'm writing an api for an IO library
  trait Writer[T] {
    def write(value: T): Unit
  }

  trait Closeable {
    def close(status: Int): Unit
  }

  trait GenericStream[T] {
    //some methods...
    def foreach(f: T => Unit): Unit
  }

  // stream, in this case, is our own type
  def processStream[T](stream: GenericStream[T] with Writer[T] with Closeable): Unit = {
    stream.foreach(println)
    stream.close(0)
  }

  // inheritance: diamond problem
  trait Animal { def name: String }
  trait Lion extends Animal { override def name: String = "Lion" }
  trait Tiger extends Animal { override def name: String = "Tiger" }
  // trait Mutant extends Lion with Tiger // that's ok
  // and if I make the Mutant a class I'll need to implement the name method
  //  class Mutant extends Lion with Tiger {
  //    override def name: String = "Mutant"
  //  }
  // but if we implement in the traits above the method name, we'll have a diamond problem
  // in a sense that if NOW I make the trait Mutant a class, I won't have to implement the name method
  class Mutant extends Lion with Tiger
  val m = new Mutant
  println(m.name) // Tiger
  // Why Tiger? Because the last trait that I've inherited is Tiger
  /*
  Mutant extends Animal with { override def name: String = "Lion" }
  with Animal with { override def name: String = "Tiger" }

  LAST OVERRIDE GETS PICKED
   */

  // the super problem + type linearization
  // let's say we want to model colors
  trait Cold {
    def print = println("cold")
  }

  trait Green extends Cold {
    override def print = {
      println("green")
      super.print
    }
  }

  trait Blue extends Cold {
    override def print = {
      println("blue")
      super.print
    }
  }

  class Red {
    def print = println("red")
  }

  class White extends Red with Green with Blue {
    override def print = {
      println("white")
      super.print
    }
  }

  val color = new White
  color.print // white, blue, green, cold

  /*
  Because
  Cold = AnyRef with <Cold>
  Green = AnyRef with <Cold> with <Green>
  Blue = AnyRef with <Cold> with <Blue>
  White = AnyRef with <Red> with <Green> with <Blue> with <White> and this is called type linearization
  Red = AnyRef with <Red>
   */




}
