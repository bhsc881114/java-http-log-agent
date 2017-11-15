package com.taovip.agent.http;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class RemoveFileHandler extends AbstractHandler {

  @Override
  public void handle(HttpExchange t) throws IOException {
    try {
      Map<String, String> parameter = SimpleHttpServer.queryToMap(t);
      String filePath = parameter.get("filePath");
      if (StringUtils.isBlank(filePath)) {
        SimpleHttpServer.writeBytes(t, "error,no filePath parameter".getBytes("UTF-8"));
        return;
      }
      try {
        String result = "";
        if (tailService.removeFile(filePath)) {
          result = "success";
        } else {
          result = "false";
        }
        SimpleHttpServer.writeBytes(t, result.getBytes("UTF-8"));
      } catch (Exception e) {
        logger.warn("", e);
        SimpleHttpServer.writeBytes(t, "error,remove file exception".getBytes("UTF-8"));
        return;
      }
    } catch (Exception e) {
      logger.warn("exception", e);
    }
  }

}
