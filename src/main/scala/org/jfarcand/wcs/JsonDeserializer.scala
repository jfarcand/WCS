package org.jfarcand.wcs

import com.wordnik.swagger.core.util.JsonUtil

class JsonDeserializer extends Deserializer {
  def deserialize(str: String, cls: Class[_]) = {
    JsonUtil.getJsonMapper.readValue(str, cls)
  }
}