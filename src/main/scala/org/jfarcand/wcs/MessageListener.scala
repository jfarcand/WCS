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

/**
 * A listener for WebSocket events.
 */
trait MessageListener {
  /**
   * Called when the {@link WebSocket} is opened
   */
  def onOpen() {}
  /**
   * Called when the {@link WebSocket} is closed
   */
  def onClose() {}
  /**
   * Called when an unexpected error occurd on a {@link WebSocket}
   */
  def onError(t: Throwable) {}
  /**
   * Called when a text message is received
   */
  def onMessage(message: String) {}
  /**
   * Called when a binary message is received.
   */
  def onMessage(message: Array[Byte]) {}
}


