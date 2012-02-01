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

import com.ning.http.client.websocket.{WebSocketTextListener, WebSocketUpgradeHandler}
import com.ning.http.client.{AsyncHttpClientConfig, AsyncHttpClient}
import collection.mutable.ListBuffer
import scala.Predef._

class WebSocket(o: Options) {

  def this() = this (null)

  val config: AsyncHttpClientConfig.Builder = new AsyncHttpClientConfig.Builder
  val asyncHttpClient: AsyncHttpClient = new AsyncHttpClient(config.build)
  val deserializers = ListBuffer[Deserializer[_]]()
  val serializers = ListBuffer[Serializer[_]]()

  var webSocket: com.ning.http.client.websocket.WebSocket = null
  var webSocketListener: WebSocketTextListener = new Wrapper(new MessageListener[String]() {
    override def onMessage(s: String) {
    }
  }, deserializers)

  def deserializer(d: Deserializer[_]): WebSocket = {
    deserializers.append(d)
    this
  }

  def serializer(d: Serializer[_]): WebSocket = {
    serializers.append(d)
    this
  }

  def open(s: String): WebSocket = {
    if (deserializers.size == 0) {
      deserializer(new Deserializer[String]() {
        def deserialize(str: String): String = {
          return str;
        }
      })
    }
    webSocket = asyncHttpClient.prepareGet(s).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(webSocketListener).build).get
    this
  }

  def close(): WebSocket = {
    webSocket.close();
    asyncHttpClient.close()
    this
  }

  def listener(l: MessageListener[_]): WebSocket = {
    if (webSocket.isOpen) {
      webSocket.addMessageListener(new Wrapper(l, deserializers))
    } else {
      webSocketListener = new Wrapper(l, deserializers);
    }

    this
  }

  def send(s: String): WebSocket = {
    webSocket.sendTextMessage(s)
    this
  }
}

private class Wrapper(l: MessageListener[_], deserializers: ListBuffer[Deserializer[_]]) extends WebSocketTextListener {

  override def onOpen(websocket: com.ning.http.client.websocket.WebSocket) {
    l.onOpen()
  }

  override def onClose(websocket: com.ning.http.client.websocket.WebSocket) {
    l.onClose()
  }

  override def onError(t: Throwable) {
    l.onError(t)
  }

  override def onMessage(s: String) {
    val objs = deserializers.filter(d => matchd(l, d))
    objs.size match {
      case 0 => //oops, nothing can do it
      case _ => l.onMessage(objs(0).deserialize(s))
    }
  }

  def matchd[T: Manifest, U: Manifest](m: MessageListener[T], d: Deserializer[U]): Boolean = {
    manifest[T] <:< manifest[U]
  }

  override def onFragment(fragment: String, last: Boolean) {}
}


