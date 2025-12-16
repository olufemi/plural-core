package com.finacial.wealth.backoffice.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static String safeToJson(Object o) {
    try { return MAPPER.writeValueAsString(o); }
    catch (Exception e) { return "{\"error\":\"json\"}"; }
  }
}
