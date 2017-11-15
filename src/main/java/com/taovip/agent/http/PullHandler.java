package com.taovip.agent.http;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sun.net.httpserver.HttpExchange;
import com.taovip.agent.domain.LineEntry;
import com.taovip.agent.util.JsonUtil;

@SuppressWarnings("restriction")
public class PullHandler extends AbstractHandler {

  @Override
  public void handle(HttpExchange t) throws IOException {
    try {
      Map<String, List<LineEntry>> lineContents = null;
      Map<String, String> parameter = SimpleHttpServer.queryToMap(t);
      String filePath = parameter.get("filePath");

      if (StringUtils.isNotBlank(filePath)) {
        lineContents = tailService.pullWithLogNames(StringUtils.split(filePath, ";"));
      } else {
        lineContents = tailService.pullAllLogs();
      }
      String res = "";
      if (lineContents.size() > 0) {
        res = JsonUtil.gson.toJson(lineContents);
      } else {
        res = "{}";
      }
      byte[] bytes = res.getBytes("UTF-8");
      SimpleHttpServer.writeBytes(t, bytes);

      if (lineContents.size() > 0) {
        for (Map.Entry<String, List<LineEntry>> entry : lineContents.entrySet()) {
          List<LineEntry> lines = entry.getValue();
          tailService.setPos(entry.getKey(), lines.get(lines.size() - 1).getPos());
        }
      }
    } catch (Exception e) {
      logger.warn("exception", e);
    }
  }
}
