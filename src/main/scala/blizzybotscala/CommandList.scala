package blizzybotscala


/**
 * Created by Anthony on 6/16/2015.
 */


class CommandList(args:String,room:String,user:String) {
  val commands = Map[String, () => String](
    "hello" -> hello,
    "echo" -> echo
  )
  def hello(): String = {
    return s"Hello, $args"
  }
  def echo(): String = {
    return args
  }
}
