package org.jfarcand.wcs

import com.wordnik.swagger.core.util.JsonUtil

class JsonSerializer extends Serializer {
  def serialize(obj: Any): String = {
    JsonUtil.getJsonMapper.writeValueAsString(obj)
  }
}