package com.taovip.agent.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.taovip.agent.domain.LineEntry;
import com.taovip.agent.domain.PosEntry;
import com.taovip.agent.tailer.AdvancedTailer;
import com.taovip.agent.tailer.AdvancedTailerListener;
import com.taovip.agent.util.FileUtil;
import com.taovip.agent.util.WriteUtil;

public class TestTailFile {

  private static final int ONE_K = 1024;
  private static String FILE_PATH = "/home/admin/a.log";
  private static final String RAF_MODE = "r";
  private final byte inbuf[] = new byte[1024];

  AdvancedTailerListener listener = new AdvancedTailerListener() {
    int i = 0;

    @Override
    public void init(AdvancedTailer tailer) {
      print("start file:" + FILE_PATH + "," + tailer.getPosition());
    }

    @Override
    public void pull(List<LineEntry> list, int lines) {

    }

    @Override
    public void handle(Exception ex) {
      ex.printStackTrace();
    }

    @Override
    public void handle(String line, long pos) {
      print("handle :" + line + "," + pos);
//      Assert.assertEquals(line, "line:" + i);
//      i++;
    }

    @Override
    public void fileRotated(long pos) {
      print("file rotated:" + FILE_PATH + "," + pos);
      i = 0;
    }

    @Override
    public void fileNotFound() {
      print("file not found:" + FILE_PATH);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    @Override
    public int getQueueSize() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public long getLineCount() {
      // TODO Auto-generated method stub
      return 0;
    }
  };

  @Test
  public void testTail() throws Exception {
    File file = new File(FILE_PATH);
    Executor executor = Executors.newSingleThreadExecutor();
    PosEntry posEntry = new PosEntry();
    posEntry.setFilePath(FILE_PATH);
    posEntry.setInode(FileUtil.getFileInode(FILE_PATH));

    AdvancedTailer tailer = new AdvancedTailer(file, posEntry, listener, 1000, false, 50 * ONE_K);
    // executor.execute(tailer);
    tailer.run();
    // WriteUtil.write(FILE_PATH, 1000, false);
  }

  @Test
  public void testRotate() throws Exception {
    testTail();

    WriteUtil.write(FILE_PATH, 1000, false);
  }

  public static void print(String str) {
    System.out.println(str);
  }

  @Test
  public void testRotateBiggerFile() throws Exception {
    String path = "/tmp/1.log";
    File file = new File(path);
    long position = 0;
    RandomAccessFile reader = new RandomAccessFile(file, RAF_MODE);
    long last = System.currentTimeMillis();
    reader.seek(position);
    position = readLines(reader);

    boolean newer = FileUtils.isFileNewer(file, last); // IO-279, must be done first
    long length = file.length();

    // File was not rotated
    // See if the file needs to be tailer again
    if (length > position) {
      // The file has more content than it did last time
      position = readLines(reader);
      last = System.currentTimeMillis();
    }

  }

  private long readLines(final RandomAccessFile reader) throws IOException {
    ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(64);
    long pos = reader.getFilePointer();
    long rePos = pos; // position to re-tailer
    int num;
    boolean seenCR = false;
    while (((num = reader.read(inbuf)) != -1)) {
      for (int i = 0; i < num; i++) {
        // rePos每次读取一行才会改变,如果没有一行这里break也不会有影响
        final byte ch = inbuf[i];
        switch (ch) {
          case '\n':
            seenCR = false; // swallow CR before LF
            rePos = pos + i + 1; //because i start from 0
            String line = new String(lineBuf.toByteArray(), "utf-8");
            listener.handle(line, rePos);
            lineBuf.reset();
            break;
          case '\r':
            if (seenCR) {
              lineBuf.write('\r');
            }
            seenCR = true;
            break;
          default:
            if (seenCR) {
              seenCR = false; // swallow final CR
              rePos = pos + i + 1;
              String line2 = new String(lineBuf.toByteArray(), "utf-8");
              listener.handle(line2, rePos);
              lineBuf.reset();
            }
            lineBuf.write(ch);
        }
      }
      pos = reader.getFilePointer();
    }
    IOUtils.closeQuietly(lineBuf); // not strictly necessary
    reader.seek(rePos); // Ensure we can re-tailer if necessary
    return rePos;
  }
}
