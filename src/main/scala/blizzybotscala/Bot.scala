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

      def login(name:String, pass:String, key:String, challenge:String, ws:WebSocket): Unit = {
        if (pass eq "") {
          // If the pass == '', then we need to use a GET request, since the account is unregistered.
          val get_url = url + s"?act=getassertion&userid=$name&challengekeyid=$key&challenge=$challenge"
          val response = Http (get_url).asString
          ws.sendTextMessage (s"|/trn $name,0,${response.body}")
        } else {
          val response = Http (url).postForm (Seq ("act" -> "login", "name" -> name, "pass" -> pass, "chellengekeyid" -> key, "challenge" -> challenge) ).asString
          val json = response.toString.split ("]") (1)
          // TODO
        }
      }

      def send_command(messages:Array[String]): Unit = {
        val room = messages(0)
        val user = messages(3)
        // If the message starts with '$'
        if (messages(4).charAt(0).equals('$')) {
          val cmd = messages(4).split('$')(1).split(" ")(0)
          var args:String = null
          try {
            args = messages(4).split(s"$cmd ")(1)
          } catch {
            case e:ArrayIndexOutOfBoundsException =>
              args = ""
          }
          // We apply the command as a method then send it to the websocket as a message.
          ws.sendTextMessage(s"$room|${new CommandList(args, room, user).commands(cmd.toLowerCase()).apply()}")
      }
      override def onMessage(m:String): Unit = {

        // Remove the newline and '>' from the message.
        val message = m.replaceAll("^>","").replace("\n", "").toLowerCase()
        println(message)
        var messages = new Array[String](0)
        try {
          messages = message.split('|')
        } catch {
          case e:Exception => println(e)
        }
        messages(1) match {
            // challstr: we use this to login.
          case "challstr" =>
            val key = messages(2)
            val challenge = messages(3)
            this.login(name, pass, key, challenge, ws)
          case "updateuser" => {
            // We then join every room
            for (room <- rooms) {
              ws.sendTextMessage(s"|/join $room")
            }
          }
          case "c:" => {
            this.send_command(messages)
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
