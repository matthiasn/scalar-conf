package example

import scala.scalajs.js
import js.Dynamic.{ global => g, literal => lit}
import shared.ChatMsg

object ReactComponents {
  val DOM = g.React.DOM
  def timeFormat(ts: String): String = g.moment(ts).format("HH:mm:ss").toString

  val ChatMsgComp = g.React.createClass(lit(
    render = {
      ths: js.Dynamic => {
        val msg = ths.props.chatMsg.asInstanceOf[ChatMsg]
        val cssClass = msg.name match {
          case "Server" => "msg server"
          case "Juliet" => "msg juliet"
          case _ if ThatsApp.nameStack.head == msg.name => "msg"
          case _ => "msg others"
        }
        DOM.div(lit(className = cssClass), timeFormat(msg.timestamp), " - ", msg.name, ": ", msg.text)
      }
    }: js.ThisFunction)
  )

  val ChatMsgsComp = g.React.createClass(lit(
    render = {
      ths: js.Dynamic => {
        val messages = ths.props.messages.asInstanceOf[Vector[ChatMsg]]
        val divs = messages.map { msg => ChatMsgComp(lit(chatMsg = msg.asInstanceOf[js.Any])) }.toArray
        DOM.div(null, divs)
      }
    }: js.ThisFunction)
  )

  val NameBoxComp = g.React.createClass(lit(
    render = {
      ths: js.Dynamic => {
        DOM.span(null,
          "Who are you? ",
          DOM.input(lit(`type`="text", value = ths.props.name, id="nameField", onChange = ths.props.handler)),
          DOM.input(lit(`type`="button", value = "undo", onClick = ths.props.undo))
        )
      }
    }: js.ThisFunction)
  )
}
