package blizzybotscala

/**
 * Created by Anthony on 4/23/2015.
 */
object App {
  def main(args: Array[String]): Unit = {
    val bot = new Bot("botname", "botpass", "server:8000", List("rooms"))
    bot.connect()
  }

}
