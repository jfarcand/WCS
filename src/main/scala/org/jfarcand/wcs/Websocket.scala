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
import collection.mutable.ListBuffer
import org.slf4j.{Logger, LoggerFactory}

/**
 * Simple WebSocket Fluid Client API
 * <pre>  Websocket().open("ws://localhost".send("Hello").listener(new MyListener() {...}).close </pre>
 */
object WebSocket {
  val listeners: ListBuffer[WebSocketListener] = ListBuffer[WebSocketListener]()
  var config: AsyncHttpClientConfig.Builder = new AsyncHttpClientConfig.Builder
  var asyncHttpClient: AsyncHttpClient = new AsyncHttpClient(config.build)

  def apply(o: Options): WebSocket = {
    if (o != null) {
      config.setRequestTimeoutInMs(o.idleTimeout).setUserAgent(o.userAgent)
    }

    try {
      asyncHttpClient = new AsyncHttpClient(config.build)
    } catch {
      case t: IllegalStateException => {
        config = new AsyncHttpClientConfig.Builder
      }
    }
    new WebSocket(o, None, false, asyncHttpClient, listeners)
  }

  def apply(): WebSocket = {
    apply(new Options)
  }
}

case class WebSocket(o: Options,
                     webSocket: Option[com.ning.http.client.websocket.WebSocket],
                     isOpen: Boolean,
                     asyncHttpClient: AsyncHttpClient,
                     listeners: ListBuffer[WebSocketListener]) {

  val logger: Logger = LoggerFactory.getLogger(classOf[WebSocket])

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

    logger.trace("Opening to {}", s)
    new WebSocket(o, Some(asyncHttpClient.prepareGet(s).execute(b.build).get), true, asyncHttpClient, listeners)
  }

  /**
   * Close a WebSocket connection.
   */
  def close: WebSocket = {
    logger.trace("Closing")

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
   * Remove a {@link MessageListener}
   */
  def removeListener(l: MessageListener): WebSocket = {
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
      webSocket.get.removeWebSocketListener(wrapper)
    } else {
      listeners -= wrapper
    }
    this
  }

  /**
   * Send a text message.
   */
  def send(s: String): WebSocket = {
    if (!isOpen) throw new WebSocketException("Not Connected", null)

    logger.trace("Sending to {}", s)

    webSocket.get.sendTextMessage(s)
    this
  }

  /**
   * Send a byte message.
   */
  def send(s: Array[Byte]): WebSocket = {
    if (!isOpen) throw new WebSocketException("Not Connected", null)

    logger.trace("Sending to {}", s)

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

  override def hashCode() = l.hashCode

  override def equals(o: Any): Boolean = {
    o match {
      case t: TextListenerWrapper => t.hashCode.equals(this.hashCode)
      case _ => return false
    }
  }
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

  override def hashCode() = l.hashCode

  override def equals(o: Any): Boolean = {
    o match {
      case t: BinaryListenerWrapper => t.hashCode.equals(this.hashCode)
      case _ => return false
    }
  }

  override def onFragment(fragment: Array[Byte], last: Boolean) {}
}

