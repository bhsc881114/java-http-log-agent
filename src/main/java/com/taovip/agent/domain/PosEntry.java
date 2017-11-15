package com.taovip.agent.domain;

/**
 * Created by chentao on 16/9/18.
 */
public class PosEntry {

  private long pos;
  private String filePath;
  private String inode;
  private String charset;
  private long lastReadtime;

  public PosEntry clone() {
    PosEntry entry = new PosEntry();
    entry.filePath = this.filePath;
    entry.inode = this.inode;
    entry.pos = this.pos;
    entry.charset = this.charset;
    entry.lastReadtime = this.lastReadtime;
    return entry;
  }

  public String getCharset() {
    return charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getInode() {
    return inode;
  }

  public void setInode(String inode) {
    this.inode = inode;
  }

  public long getLastReadtime() {
    return lastReadtime;
  }

  public void setLastReadtime(long lastReadtime) {
    this.lastReadtime = lastReadtime;
  }

  public long getPos() {
    return pos;
  }

  public void setPos(long pos) {
    this.pos = pos;
    this.lastReadtime = System.currentTimeMillis();
  }
}
