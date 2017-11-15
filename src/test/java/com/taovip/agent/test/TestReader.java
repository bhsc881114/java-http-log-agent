package com.taovip.agent.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.taovip.agent.SimpleFileReader;
import com.taovip.agent.domain.Constants;
import com.taovip.agent.domain.FileEntry;

public class TestReader {

  public static Log logger = LogFactory.getLog("agentLog2");

  @Test
  public void testStartReader() throws Exception {
    new Thread() {
      public void run() {
        intervalNginxRequest();
      }
    }.start();

    List<FileEntry> fileEntrys = new ArrayList<FileEntry>();
    fileEntrys.add(new FileEntry("/Users/www1/logs/test1.log", "UTF-8"));
    fileEntrys.add(new FileEntry("/Users/www1/logs/test2.log", "UTF-8"));

    SimpleFileReader.start(fileEntrys, Constants.PORT);
    Thread.sleep(1000000);
  }

  @Test
  public void testPullLog() throws IOException, InterruptedException {
    while (true) {
      System.out.println(sendGet("http://localhost:" + Constants.PORT + Constants.HTTP_PATH, ""));

      Thread.sleep(2000);
    }
  }


  @Test
  public void testWriteLog() throws IOException, InterruptedException {
    while (true) {
      logger.warn("我是中文");
      logger.warn("i am english");
      Thread.sleep(2000);
    }
  }

  @Test
  public void testPullLogFile() throws IOException, InterruptedException {
    while (true) {
      System.out.println(sendGet("http://localhost:" + Constants.PORT + Constants.HTTP_PATH,
          "filePath=/Users/www1/logs/test1.log"));

      Thread.sleep(2000);
    }
  }

  public void intervalNginxRequest() {
    String[] urls = new String[]{"http://localhost/test1.html", "http://localhost/test2.html"};
    while (true) {
      for (String url : urls) {
        sendGet(url, "param");
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static String sendGet(String url, String param) {
    String result = "";
    BufferedReader in = null;
    try {
      String urlNameString = url + "?" + param;
      URL realUrl = new URL(urlNameString);

      URLConnection connection = realUrl.openConnection();
      connection.setRequestProperty("accept", "*/*");
      connection.setRequestProperty("connection", "Keep-Alive");
      connection.setRequestProperty("user-agent",
          "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
      connection.connect();

      in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      while ((line = in.readLine()) != null) {
        result += line;
      }
    } catch (Exception e) {
      System.out.println("get exception" + e);
      e.printStackTrace();
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (Exception e2) {
        e2.printStackTrace();
      }
    }
    return result;
  }
}
