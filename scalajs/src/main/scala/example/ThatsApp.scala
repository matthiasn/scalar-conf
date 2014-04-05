package example

import scala.collection.mutable.Stack
import scala.scalajs.js
import js.Dynamic.{ global => g, newInstance => jsnew, literal => lit}
import scala.scalajs.js.annotation.JSExport
import org.scalajs.spickling._
import org.scalajs.spickling.jsany._
import org.scalajs.jquery.{jQuery => jQ}
import scala.scalajs.js.JSON

import shared.ChatMsg
import Implicits._

@JSExport
object ThatsApp {
  @JSExport
  def main(): Unit = { }

  var counter = 0
  def now = jsnew(g.Date)()

  var msgs = Vector[ChatMsg]()

  PicklerRegistry.register[ChatMsg]
  PicklerRegistry.register[Vector[ChatMsg]]

  val tlComp = g.React.renderComponent(ReactComponents.ChatMsgsComp(
    lit(messages = msgs.asInstanceOf[js.Any])), g.document.getElementById("chat"))

  /** stack holding name and name changes over time as a simple example of undoable app state */
  val nameStack = Stack[String]("Matthias")

  /** pop stack, trigger re-render */
  def undoHandler = (e: js.Dynamic) => { if (nameStack.size > 1) { nameStack.pop(); setName() } }

  /** set name field with head of stack */
  def setName(): Unit = nameBox.setProps(lit(name = nameStack.head))
  val changeHandler: js.Function1[js.Dynamic, Unit] = (e: js.Dynamic) => {
    nameStack.push(e.target.value.toString)
    setName()
  }

  /** react component for holding user name (changes undoable) */
  val nameBox = g.React.renderComponent(ReactComponents.NameBoxComp(
    lit(name = nameStack.head, handler = changeHandler, undo = undoHandler)), g.document.getElementById("name-box"))

  def addMsg(obj: js.Dynamic) = {
    val parsed : js.Any = JSON.parse(obj.data.toString)
    val msg = PicklerRegistry.unpickle(parsed).asInstanceOf[ChatMsg]
    msgs = msg +: msgs
    tlComp.setProps(lit(messages = msgs.take(8).asInstanceOf[js.Any]))
  }

  def addPrev(data: js.Any) = {
    PicklerRegistry.unpickle(data) match {
      case v: Vector[ChatMsg] => 
        msgs = v
        tlComp.setProps(lit(messages = msgs.asInstanceOf[js.Any]))
    }
  }

  val nameField = jQ("#nameField")
  val textField = jQ("#textField")
  
  @JSExport
  def submit(): Unit = {
    Ajax.post(PicklerRegistry.pickle((ChatMsg(textField.value().toString, nameField.value().toString, now.toString, counter))))
    counter = counter + 1
  }

  val stream = jsnew(g.EventSource)("/chatFeed")
  stream.addEventListener("message", addMsg _)

  Ajax.loadPrev()
}
