package com.taovip.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taovip.agent.core.TailerService;
import com.taovip.agent.domain.Constants;
import com.taovip.agent.domain.FileEntry;
import com.taovip.agent.http.SimpleHttpServer;

/**
 * fuck flume
 *
 * @author chentao
 */
public class SimpleFileReader {

  private static final Log logger = LogFactory.getLog(SimpleFileReader.class);

  private static SimpleHttpServer httpServer;
  private static TailerService tailerService = TailerService.getINSTANCE();

  static {
    shutDownHook();
    makeSureRootPath();
  }

  public static void main(String[] args) throws Exception {
    final String filePaths = args[0];
    if (StringUtils.isBlank(filePaths)) {
      throw new Exception("file can't be null");
    }
    String[] files = StringUtils.split(filePaths, ",");
    int httpPort = args.length >= 2 ? Integer.parseInt(args[1]) : Constants.PORT;
    String charset = "UTF-8";
    if (args.length >= 3) {
      charset = args[2];
    }
    List<FileEntry> fileEntrys = new ArrayList<FileEntry>(files.length);
    for (String file : files) {
      FileEntry fileEntry = new FileEntry(file, charset);
      fileEntrys.add(fileEntry);
    }
    start(fileEntrys, httpPort);
  }

  public static void start(List<FileEntry> fileEntrys, int httpPort) throws Exception {
    // start tailer file
    tailerService.start();
    for (FileEntry fileEntry : fileEntrys) {
      tailerService.addFile(fileEntry);
    }
    // start http server
    httpServer = new SimpleHttpServer(httpPort);
    httpServer.start();
    logger.info("start success");
  }

  private static void makeSureRootPath() {
    File file = new File(Constants.PATH);
    if (file.isFile()) {
      logger.warn(Constants.PATH + " should be directory");
      throw new RuntimeException(Constants.PATH + " should be directory");
    } else {
      file.mkdir();
    }
  }

  private static void shutDownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        logger.info("going to stop:" + System.currentTimeMillis());

        httpServer.stop();
        tailerService.stop();

        logger.info("stop:" + System.currentTimeMillis());
      }
    });
  }

}

