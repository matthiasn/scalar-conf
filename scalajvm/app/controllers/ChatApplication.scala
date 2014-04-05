package controllers

import play.api.mvc._
import shared.ChatMsg
import play.api.libs.json.JsValue
import org.scalajs.spickling._
import org.scalajs.spickling.playjson._
import play.api.libs.iteratee.{Enumeratee, Concurrent}
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import Implicits._

object ChatApplication extends Controller {

  PicklerRegistry.register[ChatMsg]
  PicklerRegistry.register[Vector[ChatMsg]]

  /** Central hub for distributing chat messages */
  val (chatOut, chatChannel) = Concurrent.broadcast[ChatMsg]

  /** messages stored in an immutable vector (overwritten when new messages received) */
  var msgs = Vector[ChatMsg]()

  def index = Action { Ok(views.html.index("")) }

  /** add message */
  def addMsg(msg: ChatMsg): Unit = {
    chatChannel.push(msg)
    msgs = msg +: msgs
  }
  
  /** Controller action for POSTing chat messages */
  def postMessage = Action(parse.json) {
    req => {
      val msg = PicklerRegistry.unpickle(req.body).asInstanceOf[ChatMsg]
      addMsg(msg)
      if (msg.seq == 0) { addMsg(ChatMsg("Hello " + msg.name, "Server", msg.timestamp, 1)) }
      Ok("")
    }
  }

  /** Controller action for retrieving chat messages */
  def getMessages = Action { Ok(PicklerRegistry.pickle(msgs.take(8))) }

  /** Enumeratee for converting typed chat messages to JsValue */
  def chatMsg2JsValue: Enumeratee[ChatMsg, JsValue] = Enumeratee.map[ChatMsg] { msg => PicklerRegistry.pickle(msg) }

  /** Controller action serving activity based on room */
  def chatFeed = Action { req =>
    println("connection established")
    Ok.feed(chatOut
      &> Concurrent.buffer(50)
      &> chatMsg2JsValue
      &> EventSource()
    ).as("text/event-stream")
  }
}
