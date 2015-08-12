/*
* Copyright 2012 Jeanfrancois Arcand
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

package org.jfarcand.wcs.test

import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, TimeUnit}
import javax.servlet.http.HttpServletRequest

import org.jfarcand.wcs._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers => ShouldMatchers}

@RunWith(classOf[JUnitRunner])
class WebSocketTest extends BaseTest with ShouldMatchers {

  private final class EchoTextWebSocket extends org.eclipse.jetty.websocket.WebSocket with org.eclipse.jetty.websocket.WebSocket.OnTextMessage with org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage {
    private var connection: org.eclipse.jetty.websocket.WebSocket.Connection = null

    def onOpen(connection: org.eclipse.jetty.websocket.WebSocket.Connection): Unit = {
      this.connection = connection
      connection.setMaxTextMessageSize(1000)
    }

    def onClose(i: Int, s: String): Unit = connection.close()

    def onMessage(s: String): Unit = {
      try {
        connection.sendMessage(s)
      } catch {
        case e: IOException =>
          try {
            connection.sendMessage("FAIL")
          } catch {
            case e1: IOException => e1.printStackTrace()
          }
      }
    }

    def onMessage(data: Array[Byte], offset: Int, length: Int): Unit = {
      try {
        connection.sendMessage(data, offset, length)
      } catch {
        case e: IOException =>
          try {
            connection.sendMessage("FAIL")
          } catch {
            case e1: IOException => e1.printStackTrace()
          }
      }
    }
  }

  def getWebSocketHandler: BaseTest#WebSocketHandler = new WebSocketHandler {
    def doWebSocketConnect(httpServletRequest: HttpServletRequest, s: String): org.eclipse.jetty.websocket.WebSocket = {
      new EchoTextWebSocket
    }
  }

  it should "send a text message" in {
    val w = WebSocket()

    var s = ""
    val latch: CountDownLatch = new CountDownLatch(1)
    w.open(getTargetUrl).listener(new TextListener {

      override def onMessage(message: String) {
        s = message
        latch.countDown()
      }

    }).send("foo")

    latch.await()
    assert(s === "foo")
  }

  it should "send a byte message" in {
    val w = WebSocket()

    var s = ""
    val latch: CountDownLatch = new CountDownLatch(1)
    w.open(getTargetUrl).listener(new BinaryListener {

      override def onMessage(message: Array[Byte]) {
        s = new String(message)
        latch.countDown()
      }

    }).send("foo".getBytes)

    latch.await()
    assert(s === "foo")
  }

  it should "wait for an open event" in {
    val w = WebSocket()

    var s: Boolean = false
    val latch: CountDownLatch = new CountDownLatch(1)
    w.listener(new TextListener {

      override def onOpen {
        s = true
        latch.countDown()
      }

    }).open(getTargetUrl)

    latch.await()
    assert(s)
  }

  it should "wait for an open event with listener added after the open" in {
    val w = WebSocket()

    var s: Boolean = false
    val latch: CountDownLatch = new CountDownLatch(1)
    w.open(getTargetUrl).listener(new TextListener() {

      override def onOpen {
        s = true
        latch.countDown()
      }

    })

    latch.await()
    assert(s)
  }

  it should "wait for an close event with code" in {
    var w = WebSocket()

    val s: AtomicReference[String] = new AtomicReference[String]
    val latch: CountDownLatch = new CountDownLatch(1)
    w = w.listener(new TextListener {

      override def onMessage(message: String) {
        w.close
      }

      override def onClose(code :Int, reason : String) {
        s.set(code + "-" + reason)
        latch.countDown()
      }

    }).open(getTargetUrl).send("foo")

    latch.await()
    assert(s.get() == "1000-Normal closure; the connection successfully completed whatever purpose for which it was created.")
  }


  it should "wait for an close event" in {
    var w = WebSocket()

    var s: Boolean = false
    val latch: CountDownLatch = new CountDownLatch(1)
    w = w.listener(new TextListener {

      override def onMessage(message: String) {
        w.close
      }

      override def onClose {
        s = true
        latch.countDown()
      }

    }).open(getTargetUrl).send("foo")

    latch.await()
    assert(s)
  }

  it should "open with an Option" in {
    val o = new Options
    o.userAgent = "test/1.1"
    var w = WebSocket(o)

    var s: Boolean = false
    val latch: CountDownLatch = new CountDownLatch(1)
    w = w.listener(new TextListener {

      override def onMessage(message: String) {
        w.close
      }

      override def onClose {
        s = true
        latch.countDown()
      }

    }).open(getTargetUrl).send("foo")

    latch.await()
    assert(s)
  }

  it should "remove a listener" in {
    var w = WebSocket()

    var s: Boolean = false
    val latch: CountDownLatch = new CountDownLatch(1)
    val t = new TextListener {

      override def onMessage(message: String) {
        s = true
        latch.countDown()
      }

      override def onClose {
      }

    }
    w = w.listener(t).removeListener(t).open(getTargetUrl).send("foo")

    latch.await(1, TimeUnit.SECONDS)
    assert(!s)
  }

  it should "remove a listener after open" in {
    var w = WebSocket()

    var s: Boolean = false
    val latch: CountDownLatch = new CountDownLatch(1)
    val t = new TextListener {

      override def onMessage(message: String) {
        s = true
        latch.countDown()
      }

      override def onClose {
      }

    }
    w = w.listener(t).open(getTargetUrl).removeListener(t).send("foo")

    latch.await(1, TimeUnit.SECONDS)
    assert(!s)
  }

  it should "remove a listener after receiving a message" in {
    var w = WebSocket()

    val latch: CountDownLatch = new CountDownLatch(2)
    val t = new TextListener {

      override def onMessage(message: String) {
        w.removeListener(this)
        latch.countDown()
      }

      override def onClose {
      }

    }
    w = w.listener(t).open(getTargetUrl).send("foo").send("foo")

    latch.await(1, TimeUnit.SECONDS)
    assert(latch.getCount == 1)
  }
}

