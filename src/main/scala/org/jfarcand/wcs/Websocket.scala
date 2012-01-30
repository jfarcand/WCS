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
import com.ning.http.client.websocket.{WebSocketTextListener, WebSocket, WebSocketUpgradeHandler}
class Websocket(o: Options) {

  val asyncHttpClient: AsyncHttpClient = new AsyncHttpClient()
  var webSocket: WebSocket = null
  var webSocketListener: WebSocketTextListener = new Wrapper(new  WebsocketTextListener() {
        override def onMessage(s: String) {
        }
      })

  def this() = this(null)

  def open(s: String): Websocket = {
    webSocket = asyncHttpClient.prepareGet(s).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(webSocketListener).build).get

    return this
  }

  def close(): Websocket = {
    webSocket.close();
    return this
  }

  def listener(l: WebsocketTextListener): Websocket = {
    if (webSocket.isOpen) {
      webSocket.addMessageListener(new Wrapper(l))
    } else {
      webSocketListener = new Wrapper(l);
    }

    return this
  }

  def sendMessage(s: String) {
    webSocket.sendTextMessage(s)
  }
}

private class Wrapper(l: WebsocketTextListener) extends WebSocketTextListener {

  override def onOpen(websocket: WebSocket) {
    l.onOpen()
  }

  override def onClose(websocket: WebSocket) {
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

