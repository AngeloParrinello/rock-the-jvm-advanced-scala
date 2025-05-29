package lectures.part4implicits

import java.util.Date

object JSONSerialization extends App {

  /*
  Users, posts, feeds
  Serialize to JSON
   */

  case class User(name: String, age: Int, email: String)
  case class Post(content: String, createdAt: Date)
  case class Feed(user: User, posts: List[Post])

  // we need to serialize these to JSON
  // but we want that other people in the codebase can serialize other types to JSON and can use these serializers

  /*
  1 - Intermediate data types: Int, String, List, Date
  2 - type class for conversion to intermediate data types
  3 - serialize to JSON
   */

  sealed trait JSONValue { // intermediate data type, this will return a string representation of the intermediate data types
    def stringify: String
  }
  final case class JSONString(value: String) extends JSONValue {
    override def stringify: String = "\"" + value + "\"" // we simplify it without considering some edge cases
  }
  final case class JSONNumber(value: Int) extends JSONValue {
    override def stringify: String = value.toString
  }
  final case class JSONArray(values: List[JSONValue]) extends JSONValue {
    override def stringify: String = values.map(_.stringify).mkString("[", ",", "]")
  }
  final case class JSONObject(values: Map[String, JSONValue]) extends JSONValue {
    /*
    {
      name: "John"
      age: 22
      friends: [...]
      latestPost: {
        content: "Scala Rocks"
        date: ...
      }
      }
     */
    override def stringify: String =
      values.map { case (key, value) => s""""$key": ${value.stringify}""" }.mkString("{", ",", "}")
  }

  val data = JSONObject(Map(
    "user" -> JSONString("Daniel"),
    "posts" -> JSONArray(List(
      JSONString("Scala Rocks!"),
      JSONNumber(453)
    ))
  ))

  println(data.stringify)

  // type classes
  // what we need in a type classes?
  // 1 - type class itself
  // 2 - type class instances (some of them are built-in, implicits)
  // 3 - pimp library to use type class instances

  // Step 2.1 - our type class
  trait JSONConverter[T] {
    def convert(value: T): JSONValue
  }

  // Step 2.2 - type class instances

  // existing data types
  implicit object StringConverter extends JSONConverter[String] {
    override def convert(value: String): JSONValue = JSONString(value)
  }

  implicit object NumberConverter extends JSONConverter[Int] {
    override def convert(value: Int): JSONValue = JSONNumber(value)
  }

  // custom data types
  implicit object UserConverter extends JSONConverter[User] {
    override def convert(user: User): JSONValue = JSONObject(Map(
      "name" -> JSONString(user.name),
      "age" -> JSONNumber(user.age),
      "email" -> JSONString(user.email)
    ))
  }

  implicit object PostConverter extends JSONConverter[Post] {
    override def convert(post: Post): JSONValue = JSONObject(Map(
      "content" -> JSONString(post.content),
      "created" -> JSONString(post.createdAt.toString)
    ))
  }

  implicit object FeedConverter extends JSONConverter[Feed] {
    override def convert(feed: Feed): JSONValue = JSONObject(Map(
      "user" -> feed.user.toJSON, // previous version : UserConverter.convert(feed.user)
      "posts" -> JSONArray(feed.posts.map(_.toJSON)) // previous version : feed.posts.map(PostConverter.convert)
    ))
  }

  // Step 2.3 - conversion
  implicit class JSONOps[T](value: T) {
    def toJSON(implicit converter: JSONConverter[T]): JSONValue = converter.convert(value)
  }

  // call stringify on the result
  val now = new Date(System.currentTimeMillis())
  val john = User("John", 34, "john@rockthejvm.com")
  val feed = Feed(john, List(
    Post("hello", now),
    Post("look at this cute puppy", now)
  ))

  println(feed.toJSON.stringify)
}
