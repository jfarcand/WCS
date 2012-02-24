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
 * Configure the WebSocket class.
 */
class Options {

  /**
   * The maximum idle time of the websocket's connection before it gets closed
   */
  var idleTimeout = 60000
  /**
   * The WebSocket message maximum size
   */
  var maxMessageSize = 8192
  /**
   * The User Agent used by this library
   */
  var userAgent = "wCS/1.0"
  /**
   * The WebSocket protocol.
   */
  var protocol = null
}
