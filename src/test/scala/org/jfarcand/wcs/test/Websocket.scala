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

import org.jfarcand.wcs._

import java.io.IOException
import java.util.concurrent.CountDownLatch
import javax.servlet.http.HttpServletRequest

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import scala.collection.JavaConversions._

import org.jfarcand.wcs._

@RunWith(classOf[JUnitRunner])
class WebsocketTest extends BaseTest with FlatSpec with ShouldMatchers {
  private final class EchoTextWebSocket extends org.eclipse.jetty.websocket.WebSocket with org.eclipse.jetty.websocket.WebSocket.OnTextMessage {
    def onOpen(connection: org.eclipse.jetty.websocket.WebSocket.Connection): Unit = {
      this.connection = connection
      connection.setMaxTextMessageSize(1000)
    }

    def onClose(i: Int, s: String): Unit = {
      connection.close
    }

    def onMessage(s: String): Unit = {
      try {
        connection.sendMessage(s)
      } catch {
        case e: IOException => {
          try {
            connection.sendMessage("FAIL")
          } catch {
            case e1: IOException => {
              e1.printStackTrace
            }
          }
        }
      }
    }

    private var connection: org.eclipse.jetty.websocket.WebSocket.Connection = null
  }

  def getWebSocketHandler: BaseTest#WebSocketHandler = {
    return new WebSocketHandler {
      def doWebSocketConnect(httpServletRequest: HttpServletRequest, s: String): org.eclipse.jetty.websocket.WebSocket = {
        return new EchoTextWebSocket
      }
    }
  }

  it should "send a message" in {
    val w = new WebSocket();

    var s = "";
    var latch: CountDownLatch = new CountDownLatch(1)
    w.open(getTargetUrl).listener(new MessageListener() {

      override def onMessage(message: String) {
        s = message
        latch.countDown()
      }

    }).send("foo");

    latch.await()
    assert(s === "foo")
  }
}

