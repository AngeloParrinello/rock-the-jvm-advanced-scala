package lectures.part5ts

object SelfTypes extends App {

  /*
  Self types are a way to declare that a trait must be mixed into another trait, even though it is not a superclass
   */

  // we want to model an API for a band
  // and we want that every person in the group can play an instrument
  trait Instrumentalist {
    def play(): Unit
  }

  // to enforce that every singer must be an instrumentalist
  trait Singer {
    // we can name it self, this, abc, or whatever because this structure is separate from the rest of the implementation
    self: Instrumentalist => // SELF TYPE whoever implements Singer to implement Instrumentalist
    def sing(): Unit
  }

  class LeadSinger extends Singer with Instrumentalist { // valid extension
    override def play(): Unit = ???
    override def sing(): Unit = ???
  }

//  class Vocalist extends Singer { // this will not compile because Vocalist does not implement Instrumentalist
//    override def sing(): Unit = ???
//  }

  val jamesHetfield = new Singer with Instrumentalist {
    override def play(): Unit = ???
    override def sing(): Unit = ???
  }

  class Guitarist extends Instrumentalist {
    override def play(): Unit = println("guitar solo")
  }

  val ericClapton = new Guitarist with Singer {
    override def sing(): Unit = ???
  }

  // self types often compared to inheritance
  class A
  class B extends A // B IS AN A

  trait T
  trait S { self: T => } // S REQUIRES a T
  // but are two different things

  // Self types are commonly used in what is known as the
  // CAKE PATTERN (Scala equivalent to "dependency injection")
  // we enforce that a component must be mixed into a component

  // DI => "cake pattern"

  // DI => Dependency Injection
  trait Component {
    // API
  }
  class ComponentA extends Component
  class ComponentB extends Component
  class DependentComponent(val component: Component)

  // CAKE PATTERN => "layered architecture"
  // and so this syntax allow us to define methods in the upper trait and then use it in the dependent trait
  // as if it was defined in the dependent trait
  trait ScalaComponent {
    // API
    def action(x: Int): String
  }
  trait ScalaDependentComponent {
    self: ScalaComponent =>
    def dependentAction(x: Int): String = action(x) + " this rocks!" // action method is defined in ScalaComponent
  }

  // example of a backend developer for a server side app, so with Dashboard and Analytics
  // layer 1 - small components
  trait Picture extends ScalaComponent
  trait Stats extends ScalaComponent

  // layer 2 - compose
  trait Profile extends ScalaDependentComponent with Picture
  trait Analytics extends ScalaDependentComponent with Stats

  // layer 3 - app
  // now we can also define another trait that uses the Profile and Analytics
  trait ScalaApplication {
    self: ScalaDependentComponent =>
  }
  trait AnalyticsApp extends ScalaApplication with Analytics

  /*
  One of the big differences between self types and inheritance is that self types allow you to check the "dependencies"
  at compile time, while inheritance does check that at runtime
   */

  // cyclical dependencies
  //  class X extends Y
  //  class Y extends X // does not compile
  // but with self types..

  trait X { self: Y => }
  trait Y { self: X => }

  // self types are a way to require a type to be mixed in
  // self types are a powerful tool for DI
  // self types are usually found in the CAKE PATTERN



}
