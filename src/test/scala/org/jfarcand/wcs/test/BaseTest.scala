package org.jfarcand.wcs.test

import org.eclipse.jetty.server.handler.HandlerWrapper
import org.eclipse.jetty.websocket.WebSocketFactory
import org.slf4j.{ LoggerFactory, Logger }
import org.eclipse.jetty.server.nio.SelectChannelConnector
import java.net.ServerSocket
import javax.servlet.http.{ HttpServletResponse, HttpServletRequest }
import org.eclipse.jetty.server.{ Request, Server }

import org.scalatest.{ FlatSpec, BeforeAndAfterAll }
import org.scalatest.Matchers

trait BaseTest extends FlatSpec with BeforeAndAfterAll with Matchers {

  protected val server: Server = new Server()
  protected final val log: Logger = LoggerFactory.getLogger(classOf[BaseTest])
  protected var port1: Int = 0
  private var _connector: SelectChannelConnector = null

  override protected def beforeAll(): Unit =  {
    port1 = findFreePort
    _connector = new SelectChannelConnector
    _connector.setPort(port1)
    server.addConnector(_connector)
    val _wsHandler: BaseTest#WebSocketHandler = getWebSocketHandler
    server.setHandler(_wsHandler)
    server.start()
    log.info("Local HTTP server started successfully")
  }

  override protected def afterAll(): Unit = server.stop()

  abstract class WebSocketHandler extends HandlerWrapper with WebSocketFactory.Acceptor {

    def getWebSocketFactory: WebSocketFactory = {
      _webSocketFactory
    }

    override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
      if (_webSocketFactory.acceptWebSocket(request, response) || response.isCommitted) return
      super.handle(target, baseRequest, request, response)
    }

    def checkOrigin(request: HttpServletRequest, origin: String): Boolean = {
      true
    }

    private final val _webSocketFactory: WebSocketFactory = new WebSocketFactory(this, 32 * 1024)
  }

  protected def findFreePort: Int = {
    var socket: ServerSocket = null
    try {
      socket = new ServerSocket(0)
      socket.getLocalPort
    } finally {
      if (socket != null) {
        socket.close()
      }
    }
  }

  protected def getTargetUrl: String = {
    "ws://127.0.0.1:" + port1
  }

  def getWebSocketHandler: BaseTest#WebSocketHandler
}