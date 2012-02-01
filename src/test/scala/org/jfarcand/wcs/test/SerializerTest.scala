package org.jfarcand.wcs.test.serializer

import com.wordnik.swagger.core.util._

import javax.xml.bind.annotation._
import org.codehaus.jackson.annotate._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import scala.collection.JavaConversions._
import scala.reflect.BeanProperty

import org.jfarcand.wcs._

@RunWith(classOf[JUnitRunner])
class SerializerTest extends FlatSpec with ShouldMatchers {
  behavior of "Serializer"

  it should "serialize an object" in {
    val serializer = new JsonSerializer
    val simpleObject = new SimpleObject()
    simpleObject.name = "rock"
    simpleObject.value = "paper"

    val json = serializer.serialize(simpleObject)
    assert(json === """{"name":"rock","value":"paper"}""")
  }

}

class SimpleObject {
  @BeanProperty var name: String = _
  @BeanProperty var value: String = _
  @BeanProperty var date: java.util.Date = _
}