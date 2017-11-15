package com.taovip.agent.domain;

/**
 * Created by chentao on 16/9/21.
 */
public class FileEntry {

  private String filePath;
  private String charset;

  public FileEntry() {

  }

  public FileEntry(String filePath, String charset) {
    this.filePath = filePath;
    this.charset = charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getCharset() {
    return charset;
  }

  public String getFilePath() {
    return filePath;
  }
}
