package org.jfarcand.wcs.test

import org.eclipse.jetty.server.handler.HandlerWrapper
import org.eclipse.jetty.websocket.WebSocketFactory
import org.slf4j.{LoggerFactory, Logger}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import java.net.ServerSocket
import org.testng.annotations.{AfterClass, BeforeClass}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eclipse.jetty.server.{Request, Server}

abstract class BaseTest extends Server {

  abstract class WebSocketHandler extends HandlerWrapper with WebSocketFactory.Acceptor {

    def getWebSocketFactory: WebSocketFactory = {
      return _webSocketFactory
    }

    override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
      if (_webSocketFactory.acceptWebSocket(request, response) || response.isCommitted) return
      super.handle(target, baseRequest, request, response)
    }

    def checkOrigin(request: HttpServletRequest, origin: String): Boolean = {
      return true
    }

    private final val _webSocketFactory: WebSocketFactory = new WebSocketFactory(this, 32 * 1024)
  }

  protected final val log: Logger = LoggerFactory.getLogger(classOf[BaseTest])
  protected var port1: Int = 0
  private var _connector: SelectChannelConnector = null

  @AfterClass(alwaysRun = true) def tearDownGlobal: Unit = {
    stop
  }

  protected def findFreePort: Int = {
    var socket: ServerSocket = null
    try {
      socket = new ServerSocket(0)
      return socket.getLocalPort
    }
    finally {
      if (socket != null) {
        socket.close
      }
    }
  }

  protected def getTargetUrl: String = {
     "ws://127.0.0.1:" + port1;
  }

  @BeforeClass(alwaysRun = true) def setUpGlobal: Unit = {
    port1 = findFreePort
    _connector = new SelectChannelConnector
    _connector.setPort(port1)
    addConnector(_connector)
    val _wsHandler: BaseTest#WebSocketHandler = getWebSocketHandler
    setHandler(_wsHandler)
    start
    log.info("Local HTTP server started successfully")
  }

  def getWebSocketHandler: BaseTest#WebSocketHandler
}