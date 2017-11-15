package com.taovip.agent.domain;

public class LineEntry {

  private long pos;
  private String line;

  public LineEntry(String line, long pos) {
    this.line = line;
    this.pos = pos;
  }

  public String getLine() {
    return line;
  }

  public void setLine(String line) {
    this.line = line;
  }

  public long getPos() {
    return pos;
  }

  public void setPos(long pos) {
    this.pos = pos;
  }

}
