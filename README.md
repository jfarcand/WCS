### wCS: An Asynchronous WebSocket Client Library for Scala.

As simply as

     WebSocket().open("ws://localhost")
        .listener(new MessageListener {
            override def onMessage(message: String) {
                // Do something
            }
        })
        .send("Hello World")
        .send("WebSocket are cool!")
        .listener(new MessageListener {
            override def onMessage(message: Array[Byte]) {
                // Do something
            }
        })
        .send("Hello World".getBytes)

Download using Maven

     <dependency>
         <groupId>org.jfarcand</groupId>
         <artifactId>wcs</artifactId>
         <version>1.0.0-SNAPSHOT</version>
      </dependency>

or a single artifact that contains all its dependencies

     <dependency>
         <groupId>org.jfarcand</groupId>
         <artifactId>wcs-all</artifactId>
         <version>1.0.0-SNAPSHOT</version>
      </dependency>




