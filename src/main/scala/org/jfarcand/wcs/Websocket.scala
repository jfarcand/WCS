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
 * <pre> new Websocket().open("ws://localhost".send("Hello").listener(new MyListener() {...}).close() </pre>
 */
class WebSocket(o: Options) {

  def this() = this (null)

  val config: AsyncHttpClientConfig.Builder = new AsyncHttpClientConfig.Builder
  config.setUserAgent("wCS/1.0")

  if (o != null) {
    config.setRequestTimeoutInMs(o.idleTimeout)
    config.setUserAgent(o.userAgent)
  }

  val asyncHttpClient: AsyncHttpClient = new AsyncHttpClient(config.build)
  var webSocket: com.ning.http.client.websocket.WebSocket = null
  var openThrowable: Throwable = null;
  val listeners: ListBuffer[WebSocketListener] = ListBuffer[WebSocketListener]()
  var isOpen = false

  /**
   * Open a WebSocket connection.
   */
  def open(s: String): WebSocket = {

    if (!s.startsWith("ws://")) {
      throw new RuntimeException("Invalid Protocol. Only WebSocket ws:// supported" + s)
    }

    val b = new WebSocketUpgradeHandler.Builder()
    if (o != null) {
      b.setMaxTextSize(o.maxMessageSize).setMaxByteSize(o.maxMessageSize).setProtocol(o.protocol)
    }

    listeners.foreach(l => {
      b.addWebSocketListener(l)
    })

    webSocket = asyncHttpClient.prepareGet(s).execute(b.build).get()
    isOpen = true
    listeners.clear()

    this
  }

  /**
   * Close a WebSocket connection.
   */
  def close(): WebSocket = {
    webSocket.close();
    asyncHttpClient.close()
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
          webSocket = w
          super.onOpen(w)
        }
      }
    } else {
      wrapper = new BinaryListenerWrapper(l) {
        override def onOpen(w: com.ning.http.client.websocket.WebSocket) {
          webSocket = w
          super.onOpen(w)
        }
      }
    }

    if (isOpen) {
      webSocket.addWebSocketListener(wrapper)
      l.onOpen()
    } else {
      listeners.append(wrapper)
    }

    this
  }

  /**
   * Send a text message.
   */
  def send(s: String): WebSocket = {
    webSocket.sendTextMessage(s)
    this
  }

  /**
   * Send a byte message.
   */
  def send(s: Array[Byte]): WebSocket = {
    webSocket.sendMessage(s)
    this
  }
}

private class TextListenerWrapper(l: MessageListener) extends WebSocketTextListener {

  override def onOpen(w: com.ning.http.client.websocket.WebSocket) {
    l.onOpen()
  }

  override def onClose(w: com.ning.http.client.websocket.WebSocket) {
    l.onClose()
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
    l.onOpen()
  }

  override def onClose(w: com.ning.http.client.websocket.WebSocket) {
    l.onClose()
  }

  override def onError(t: Throwable) {
    l.onError(t)
  }

  override def onMessage(s: Array[Byte]) {
    l.onMessage(s)
  }

  override def onFragment(fragment: Array[Byte], last: Boolean) {}
}

