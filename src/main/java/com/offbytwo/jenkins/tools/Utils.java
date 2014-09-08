package com.offbytwo.jenkins.tools;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Created by IntelliJ IDEA.
 * User: eric
 * Date: 08/09/14
 * Time: 18:18
 */
public class Utils {

  private static ObjectMapper JSON_MAPPER;
  private static ObjectMapper XML_MAPPER;

  static {
    JSON_MAPPER = new ObjectMapper();
    JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    XML_MAPPER = new XmlMapper();
    XML_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private Utils() {
  }

  public static ObjectMapper getJsonMapper() {
    return JSON_MAPPER;
  }

  public static ObjectMapper getXmlMapper() {
    return XML_MAPPER;
  }
}
