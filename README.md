### wCS: An Asynchronous WebSocket Client Library for Scala.

A really simple WebSocket library that works with [node.js](http://nodejs.org/), [Atmosphere](https://github.com/Atmosphere/atmosphere) or any WebSocket server! As simply as

```java
     WebSocket().open("ws://localhost")
        .listener(new MessageListener {
            override def onMessage(message: String) {
                // Do something
            }
        })
        .send("Hello World")
        .send("WebSockets are cool!")
        .listener(new MessageListener {
            override def onMessage(message: Array[Byte]) {
                // Do something
            }
        })
        .send("Hello World".getBytes)
```

Download using Maven

```xml
     <dependency>
         <groupId>org.jfarcand</groupId>
         <artifactId>wcs</artifactId>
         <version>1.0.0</version>
      </dependency>
```

or a single artifact that contains all its dependencies

```xml
     <dependency>
         <groupId>org.jfarcand</groupId>
         <artifactId>wcs-all</artifactId>
         <version>1.0.0</version>
      </dependency>
```





