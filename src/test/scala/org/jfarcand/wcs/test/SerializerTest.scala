package org.jfarcand.wcs.test

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import scala.collection.JavaConversions._

import org.jfarcand.wcs._

@RunWith(classOf[JUnitRunner])
class SerializerTest extends FlatSpec with ShouldMatchers {
  behavior of "Serializer"

  it should "serialize an object" in {
    class SimpleObject(var name: String,
      var value: String,
      var date: java.util.Date = new java.util.Date) {
      def this() = this(null, null, null)
    }

    val serializer = new JsonSerializer
    val simpleObject = new SimpleObject("rock", "paper", new java.util.Date())

    val json = serializer.serialize(simpleObject)
    println(json)
  }
  
  
}