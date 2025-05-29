package lectures.part2afp

object FunctionalCollections extends App {
  /*
  A functional collection: Set
  val aSet = Set(1, 2, 3)
  Set is a collection that contains no duplicate elements, Set instances are callable (they have apply method)
  aSet(2) // true
  aSet(4) // false
  Set instances are callable like functions
  The apply method always returns a value (true or false) => this means that the sets behave like functions!
  SETS ARE FUNCTIONS!
  And indeed on the scala documentation, the Set trait extends the Function1 trait:
  trait Set[A] extends (A => Boolean) ...
   */

}
