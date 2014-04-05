package example

import scala.scalajs.js
import js.Dynamic.{literal => lit}
import org.scalajs.jquery.{jQuery => jQ, JQueryXHR, JQueryAjaxSettings}
import scala.scalajs.js.JSON

object Ajax {

  def post(obj: js.Any): Unit = {
    jQ.ajax(lit(
    url = "http://localhost:9000/chat",
    contentType = "application/json; charset=utf-8",
    data = JSON.stringify(obj),
    dataType = "json",
    `type`= "POST"
    ).asInstanceOf[JQueryAjaxSettings])
  }

  def loadPrev(): Unit = {
    jQ.ajax(lit(
      url = "http://localhost:9000/messages",
      success = {
        (data: js.Any, textStatus: js.String, jqXHR: JQueryXHR) => ThatsApp.addPrev(data)
      },
      `type` = "GET"
    ).asInstanceOf[org.scalajs.jquery.JQueryAjaxSettings])
  }
}
