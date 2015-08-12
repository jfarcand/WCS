package org.jfarcand.wcs.test

import java.util.concurrent.CountDownLatch

import org.jfarcand.wcs.{MessageListener, WebSocket}
import org.scalatest.{Matchers, FlatSpec}

class WSSTest extends FlatSpec with Matchers {

  it should "receive three messages" in {
    val w = WebSocket()
    val latch: CountDownLatch = new CountDownLatch(3)
    var messages: Seq[String] = Seq()
    w open "wss://stream.pushbullet.com/websocket/wHDLQQ4cWz7uH89MjpKh47dcc8AhyFrd" listener new MessageListener {
      override def onMessage(message: String): Unit = {
        println(message)
        messages = messages ++ Seq(message)
        latch.countDown()
      }
      override def onOpen: Unit = println("open connection")
      override def onClose: Unit = println("close connection")
      override def onClose(code: Int, reason: String): Unit = println(s"close connection, code ${code.toString}. reason: $reason")
      override def onError(t: Throwable): Unit = t.printStackTrace()
      override def onMessage(message: Array[Byte]): Unit = onMessage(new String(message))
    }
    latch.await()
    assert(messages.size == 3)
  }

}
