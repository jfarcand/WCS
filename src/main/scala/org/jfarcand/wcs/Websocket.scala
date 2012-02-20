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
package org.jfarcand.wcs

import com.ning.http.client.{AsyncHttpClientConfig, AsyncHttpClient}
import scala.Predef._
import com.ning.http.client.websocket._
import java.util.concurrent.{TimeUnit, CountDownLatch}
import collection.mutable.ListBuffer

/**
 * Simple WebSocket Fluid Client API
 * <pre> new Websocket.open("ws://localhost".send("Hello").listener(new MyListener() {...}).close </pre>
 */
object WebSocket {
  val config: AsyncHttpClientConfig.Builder = new AsyncHttpClientConfig.Builder
  config.setUserAgent("wCS/1.0")
  val asyncHttpClient: AsyncHttpClient = new AsyncHttpClient(config.build)

  def apply(o: Options): WebSocket = {
    if (o != null) config.setRequestTimeoutInMs(o.idleTimeout).setUserAgent(o.userAgent)
    new WebSocket(o, None, false, asyncHttpClient)
  }

  def apply(): WebSocket = {
    new WebSocket(null, None, false, asyncHttpClient)
  }
}

class WebSocket(o: Options, webSocket: Option[com.ning.http.client.websocket.WebSocket], isOpen: Boolean, asyncHttpClient: AsyncHttpClient) {

  val listeners: ListBuffer[WebSocketListener] = ListBuffer[WebSocketListener]()

  /**
   * Open a WebSocket connection.
   */
  def open(s: String): WebSocket = {

    if (!s.startsWith("ws://")) throw new RuntimeException("Invalid Protocol. Only WebSocket ws:// supported" + s)

    val b = new WebSocketUpgradeHandler.Builder
    if (o != null) {
      b.setMaxTextSize(o.maxMessageSize).setMaxByteSize(o.maxMessageSize).setProtocol(o.protocol)
    }

    listeners.foreach(l => {
      b.addWebSocketListener(l)
    })

    listeners.clear

    new WebSocket(o, Some(asyncHttpClient.prepareGet(s).execute(b.build).get), true, asyncHttpClient)
  }

  /**
   * Close a WebSocket connection.
   */
  def close: WebSocket = {
    webSocket.foreach(_.close)
    asyncHttpClient.close
    this
  }

  /**
   * Add a {@link MessageListener}
   */
  def listener(l: MessageListener): WebSocket = {

    var wrapper: WebSocketListener = null

    if (classOf[TextListener].isAssignableFrom(l.getClass)) {
      wrapper = new TextListenerWrapper(l) {
        override def onOpen(w: com.ning.http.client.websocket.WebSocket) {
          super.onOpen(w)
        }
      }
    } else {
      wrapper = new BinaryListenerWrapper(l) {
        override def onOpen(w: com.ning.http.client.websocket.WebSocket) {
          super.onOpen(w)
        }
      }
    }

    if (isOpen) {
      webSocket.get.addWebSocketListener(wrapper)
      l.onOpen
    } else {
      listeners.append(wrapper)
    }

    this
  }

  /**
   * Send a text message.
   */
  def send(s: String): WebSocket = {
    if (!isOpen) throw new WebSocketException("Not Connected", null)

    webSocket.get.sendTextMessage(s)
    this
  }

  /**
   * Send a byte message.
   */
  def send(s: Array[Byte]): WebSocket = {
    if (!isOpen) throw new WebSocketException("Not Connected", null)

    webSocket.get.sendMessage(s)
    this
  }
}

private class TextListenerWrapper(l: MessageListener) extends WebSocketTextListener {

  override def onOpen(w: com.ning.http.client.websocket.WebSocket) {
    l.onOpen
  }

  override def onClose(w: com.ning.http.client.websocket.WebSocket) {
    l.onClose
  }

  override def onError(t: Throwable) {
    l.onError(t)
  }

  override def onMessage(s: String) {
    l.onMessage(s)
  }

  override def onFragment(fragment: String, last: Boolean) {}
}

private class BinaryListenerWrapper(l: MessageListener) extends WebSocketByteListener {

  override def onOpen(w: com.ning.http.client.websocket.WebSocket) {
    l.onOpen
  }

  override def onClose(w: com.ning.http.client.websocket.WebSocket) {
    l.onClose
  }

  override def onError(t: Throwable) {
    l.onError(t)
  }

  override def onMessage(s: Array[Byte]) {
    l.onMessage(s)
  }

  override def onFragment(fragment: Array[Byte], last: Boolean) {}
}

