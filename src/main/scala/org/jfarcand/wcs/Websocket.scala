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

import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.websocket.{WebSocket, WebSocketTextListener, WebSocketUpgradeHandler}

class WebSocket(o: Options) {

  val asyncHttpClient: AsyncHttpClient = new AsyncHttpClient()
  var webSocket: com.ning.http.client.websocket.WebSocket = null
  var webSocketListener: WebSocketTextListener = new Wrapper(new MessageListener() {
    override def onMessage(s: String) {
    }
  })
  var serializer: Serializer = new Serializer {}
  var deserializer: Deserializer = new Deserializer {}

  def this() = this (null)

  def open(s: String): WebSocket = {
    webSocket = asyncHttpClient.prepareGet(s).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(webSocketListener).build).get
    this
  }

  def close(): WebSocket = {
    webSocket.close();
    asyncHttpClient.close()
    this
  }

  def listener(l: MessageListener): WebSocket = {
    if (webSocket.isOpen) {
      webSocket.addMessageListener(new Wrapper(l))
    } else {
      webSocketListener = new Wrapper(l);
    }

    this
  }

  def send(s: String): WebSocket = {
    webSocket.sendTextMessage(s)
    this
  }

  def serialize(s: Serializer): WebSocket = {
    serializer = s;
    this
  }

  def deserialize(s: Deserializer): WebSocket = {
    deserializer = s
    this
  }

}

private class Wrapper(l: MessageListener) extends WebSocketTextListener {

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
    l.onMessage(s)
  }

  override def onFragment(fragment: String, last: Boolean) {}
}

