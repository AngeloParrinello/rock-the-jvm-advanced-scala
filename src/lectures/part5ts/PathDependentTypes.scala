package lectures.part5ts

object PathDependentTypes extends App {

  class Outer {
    class Inner
    object InnerObject
    type InnerType
    def print(i: Inner) = println(i)
    def printGeneral(i: Outer#Inner) = println(i)
  }

  def aMethod: Int = {
    class HelperClass
    type HelperType = String

    2
  }

  // using them, how?
  // 1. per instance
  val outer = new Outer
  val inner = new outer.Inner // this is a path dependent type, outer.Inner IS A TYPE

  val oo = new Outer
  // val otherInner: oo.Inner = new outer.Inner // this will not compile, oo.Inner and outer.Inner are different types!

  outer.print(inner)
  // oo.print(inner) // this will not compile
  // these are path dependent types, they are bound to the instance of the outer class

  // all the inner type have a common supertype, the Outer#Inner
  outer.printGeneral(inner)
  oo.printGeneral(inner)

  /*
    Exercise
    you have a DB keyed by Int and a map of customers in each DB, but maybe others
   */

  /*
  use path-dependent types
  abstract type members and/or type aliases
   */

  trait ItemLike {
    type Key
  }

  trait Item[K] extends ItemLike {
    override type Key = K
  }

  trait IntItem extends Item[Int]
  trait StringItem extends Item[String]

  def get[ItemType <: ItemLike](key: ItemType#Key): ItemType = ??? // try to define a good method signature for this
  // the Key type is bound to the ItemType type

  // in a way that if you call ..
  // get[IntItem](42) // you get an IntItem
  // get[StringItem]("home") // you get a StringItem
  // get[IntItem]("scala") // this will not compile
  get[IntItem](42)

}
