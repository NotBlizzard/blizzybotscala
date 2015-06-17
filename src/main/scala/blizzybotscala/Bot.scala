package blizzybotscala

import java.util.{Timer, TimerTask}
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.websocket.{WebSocket, WebSocketTextListener, WebSocketUpgradeHandler}

import scalaj.http._




/**
 * Created by Anthony on 4/23/2015.
 */

class Bot(name: String, pass: String, var server: String, rooms:List[String]) {
  server = s"ws://$server/showdown/websocket"
  def connect() {
    val url = "http://play.pokemonshowdown.com/action.php"
    var ws:WebSocket = null
    new AsyncHttpClient().prepareGet(server)
      .execute(new WebSocketUpgradeHandler.Builder()
      .addWebSocketListener(new WebSocketTextListener() {

      override def onOpen(w:WebSocket): Unit = {
        ws = w
      }

      override def onClose(w:WebSocket): Unit = {

      }
      override def onFragment(fragment:String, last:Boolean): Unit = {

      }

      override def onError(t:Throwable): Unit = {
        println(t.getStackTrace().getClass())
      }
      override def onMessage(m:String): Unit = {

        val message = m.replaceAll("^>","").replace("\n", "").toLowerCase()
        println(message)
        var messages = new Array[String](0)
        try {
          messages = message.split('|')
        } catch {
          case e:Exception => println("IT IS "+e)
        }
        messages(1) match {
          case "challstr" =>
            val key = messages(2)
            val challenge = messages(3)
            if (pass == "") {
              val get_url = url + s"?act=getassertion&userid=$name&challengekeyid=$key&challenge=$challenge"
              val response = Http(get_url).asString
              ws.sendTextMessage(s"|/trn $name,0,${response.body}")
            } else {
              val response = Http(url).postForm(Seq("act" -> "login", "name" -> name, "pass" -> pass, "chellengekeyid" -> key, "challenge" -> challenge )).asString
              val json = response.toString.split("]")(1)
            }
            for (room <- rooms) {
              ws.sendTextMessage(s"|/join $room")
            }
          case "updateuser" => {
            for (room <- rooms) {
              ws.sendTextMessage(s"|/join $room")
            }
          }
          case "c:" => {
            val room = messages(0)
            val user = messages(3)
            if (messages(4).charAt(0).equals('$')) {
              val cmd = messages(4).split('$')(1).split(" ")(0)
              var args:String = null
              try {
                args = messages(4).split(s"$cmd ")(1)
              } catch {
                case e:ArrayIndexOutOfBoundsException =>
                  args = ""
              }
              println(s"command is '$cmd'")
              ws.sendTextMessage(s"$room|${new CommandList(args, room, user).commands(cmd.toLowerCase()).apply()}")
            }
          }
          case _ => {}

        }
      }
    }).build()).get()
    val t = new Timer()
    val task = new TimerTask {
      override def run(): Unit = {
        ws.sendPing("Hello".getBytes())
      }
    }
    t.schedule(task, 0, 10000)
  }
}
