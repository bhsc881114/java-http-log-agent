package com.taovip.agent.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.taovip.agent.core.TailerService;
import com.taovip.agent.domain.Constants;

@SuppressWarnings("restriction")
public class SimpleHttpServer {

  public static Log logger = LogFactory.getLog(SimpleHttpServer.class);

  private HttpServer server;
  private ExecutorService executor;
  public volatile boolean RUNNING = true;
  private TailerService tailService = TailerService.getINSTANCE();

  public SimpleHttpServer(int port) throws IOException {
    executor = Executors.newFixedThreadPool(2);
    server = HttpServer.create(new InetSocketAddress(port), 0);

    server.createContext(Constants.HTTP_PATH, new PullHandler());
    server.createContext("/addFile", new AddFileHandler());
    server.createContext("/removeFile", new RemoveFileHandler());
    server.createContext("/version", new HttpHandler() {
      @Override
      public void handle(HttpExchange t) throws IOException {
        writeBytes(t, Constants.VERSION.getBytes("UTF-8"));
      }
    });
    server.createContext("/listFiles", new HttpHandler() {
      @Override
      public void handle(HttpExchange t) throws IOException {
        String info = tailService.getTailInfo();
        writeBytes(t, info.toString().getBytes("UTF-8"));
      }
    });

    server.setExecutor(executor);
  }

  public void start() {
    RUNNING = true;
    server.start();
  }

  public void stop() {
    try {
      server.stop(2);// wait 2 seconds
      RUNNING = false;

      executor.shutdownNow();
    } catch (Exception e) {
      logger.warn(e);
    }
  }

  public static void writeBytes(HttpExchange t, byte[] bytes) throws IOException {
    t.sendResponseHeaders(200, bytes.length);

    OutputStream os = t.getResponseBody();
    os.write(bytes); // TODO compress
    os.close();
  }

  public static Map<String, String> queryToMap(HttpExchange t) {
    String query = t.getRequestURI().getQuery();
    Map<String, String> result = new HashMap<String, String>();
    if (StringUtils.isBlank(query)) {
      return result;
    }
    for (String param : query.split("&")) {
      String pair[] = param.split("=");
      if (pair.length > 1) {
        result.put(pair[0], pair[1]);
      } else {
        result.put(pair[0], "");
      }
    }
    return result;
  }

}
