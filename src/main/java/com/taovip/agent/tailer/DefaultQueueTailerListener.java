package com.taovip.agent.tailer;

import java.io.File;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taovip.agent.domain.LineEntry;

/**
 * put file line to LinkedBlockingDeque,max capacity 500
 *
 * @author chentao
 */
public class DefaultQueueTailerListener implements AdvancedTailerListener {

  private static Log logger = LogFactory.getLog(DefaultQueueTailerListener.class);

  private String filePath;
  private long lineCount = 0;
  private LinkedBlockingQueue<LineEntry> lineQueue = new LinkedBlockingQueue<LineEntry>(5000);

  public DefaultQueueTailerListener(File file) {
    this.filePath = file.getAbsolutePath();
  }

  @Override
  public void init(AdvancedTailer tailer) {
    logger.info("start file:" + filePath);
  }

  public void handle(String lineContent, long pos) {
    try {
      LineEntry entry = new LineEntry(lineContent, pos);
      lineQueue.put(entry);
      lineCount++;
    } catch (InterruptedException e) {
      logger.warn(e);
    }
  }

  @Override
  public void pull(List<LineEntry> list, int lines) {
    lineQueue.drainTo(list, lines);
  }

  public int getQueueSize() {
    return lineQueue.size();
  }

  @Override
  public void fileNotFound() {
    logger.info("file not found:" + filePath);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      logger.warn(e);
    }
  }

  @Override
  public void fileRotated(long pos) {
    logger.info("file try rotated:" + filePath + ",count:" + lineCount + "pos:," + pos);

    lineCount = 0;
  }

  @Override
  public void handle(Exception ex) {
    logger.warn("unknow:", ex);
  }

  public long getLineCount() {
    return lineCount;
  }

}

