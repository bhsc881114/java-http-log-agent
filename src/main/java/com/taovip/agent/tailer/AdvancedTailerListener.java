package com.taovip.agent.tailer;

import java.util.List;

import com.taovip.agent.domain.LineEntry;


/**
 * @author chentao
 * @org.apache.commons.io.input.Tailer
 */
public interface AdvancedTailerListener {

  /**
   * The tailer will call this method during construction, giving the listener a method of stopping
   * the tailer.
   *
   * @param tailer the tailer.
   */
  void init(AdvancedTailer tailer);

  /**
   * This method is called if the tailed file is not found. <p> <b>Note:</b> this is called from the
   * tailer thread.
   */
  void fileNotFound();

  /**
   * Called if a file rotation is detected.
   *
   * This method is called before the file is reopened, and fileNotFound may be called if the new
   * file has not yet been created. <p> <b>Note:</b> this is called from the tailer thread.
   */
  void fileRotated(long position);

  /**
   * Handles a line from a Tailer. <p> <b>Note:</b> this is called from the tailer thread.
   *
   * @param line the line
   * @param pos the position.
   */
  void handle(String line, long pos);

  /**
   * 拉数据
   */
  void pull(List<LineEntry> list, int lines);

  /**
   * Handles an Exception . <p> <b>Note:</b> this is called from the tailer thread.
   *
   * @param ex the exception.
   */
  void handle(Exception ex);

  int getQueueSize();

  public long getLineCount();
}

