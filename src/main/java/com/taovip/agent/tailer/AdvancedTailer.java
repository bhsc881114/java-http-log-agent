package com.taovip.agent.tailer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taovip.agent.domain.PosEntry;
import com.taovip.agent.util.FileUtil;

/**
 * @author chentao
 * @org.apache.commons.io.input.Tailer
 */
public class AdvancedTailer implements Runnable {

  private static Log logger = LogFactory.getLog(AdvancedTailer.class);

  private static final String RAF_MODE = "r";

  /**
   * Buffer on top of RandomAccessFile.
   */
  private final byte inbuf[];

  /**
   * The file which will be tailed.
   */
  private final File file;
  private final String fileName;
  private PosEntry posEntry;

  /**
   * The amount of time to wait for the file to be updated.
   */
  private final long delayMillis;

  // /**
  // * Whether to tail from the end or start of file
  // */
  // private final boolean end;

  /**
   * The listener to notify of events when tailing.
   */
  private final AdvancedTailerListener listener;

  /**
   * Whether to close and reopen the file whilst waiting for more input.
   */
  private final boolean reOpen;

  /**
   * The tailer will run as long as this value is true.
   */
  private volatile boolean run = true;

  /**
   * position within the file
   */
  private long position = 0;
  /**
   * default charset
   */
  private Charset cset = Charset.defaultCharset();

  // private static final long TEN_MINUTE = 10 * 60 * 1000;

  /**
   * Creates a Tailer for the given file, with a specified buffer size.
   *
   * @param file the file to follow.
   * @param listener the AdvancedTailerListener to use.
   * @param delayMillis the delay between checks of the file for new content in milliseconds.
   * @param reOpen if true, close and reopen the file between reading chunks
   * @param bufSize Buffer size
   */
  public AdvancedTailer(File file, PosEntry posEntry, AdvancedTailerListener listener,
      long delayMillis, boolean reOpen,
      int bufSize) {
    this.file = file;
    this.fileName = file.getName();
    this.posEntry = posEntry;
    this.delayMillis = delayMillis;
    this.inbuf = new byte[bufSize];

    // Save and prepare the listener
    this.listener = listener;
    listener.init(this);
    this.reOpen = reOpen;
    if (posEntry.getCharset() != null) {
      this.cset = Charset.forName(posEntry.getCharset());
    }
    this.position = posEntry.getPos();
  }

  /**
   * Return the file.
   *
   * @return the file
   */
  public File getFile() {
    return file;
  }

  /**
   * Return the delay in milliseconds.
   *
   * @return the delay in milliseconds.
   */
  public long getDelay() {
    return delayMillis;
  }

  /**
   * Follows changes in the file, calling the AdvancedTailerListener's handle method for each new
   * line.
   */
  public void run() {
    RandomAccessFile reader = null;
    long readCount = 0;
    try {
      long last = 0; // The last time the file was checked for changes

      // Open the file
      while (run && reader == null) {
        try {
          reader = new RandomAccessFile(file, RAF_MODE);
        } catch (FileNotFoundException e) {
          listener.fileNotFound();
        }

        if (reader == null) {
          try {
            Thread.sleep(delayMillis);
          } catch (InterruptedException e) {
          }
        } else {
          // The current position in the file
          // position = end ? file.length() : 0;
          last = System.currentTimeMillis();
          reader.seek(position);
        }
      }

      while (run) {
        readCount = readCount + 1;
        boolean newer = FileUtils.isFileNewer(file, last); // IO-279, must be done first

        // Check the file length to see if it was rotated
        long length = file.length();
        if (length < position) {
          reader = rotatedFie(length, newer, reader);
        } else {
          logger.info(
              fileName + ",file position:" + position + ",length:" + length + ",lastModified:" +
                  file.lastModified() + ",equal:" + (length == position) + ",newer:" + newer
                  + ",lastRead:" + last);
          // File was not rotated
          // See if the file needs to be tailer again
          if (length > position) {
            // try reopen file
            if (posEntry.getInode() == null) {
              reader = reopen(length, newer, reader);
            }
            // The file has more content than it did last time
            position = readLines(reader);
            last = System.currentTimeMillis();
          } else if (length == position) {
            if (newer) {
              // log4j的日志可能出现时间更新，但是length没变的情况，这种情况可能会使日志重复
              logger.info(fileName + ":impossible:" + position + "," + length + ","
                  + file.lastModified() + "," + (length > position) + "," + newer + "," + last);
            }
          } else if (newer) {
            /*
             * This can happen if the file is truncated or overwritten with the exact same length of
             * information. In cases like this, the file position needs to be reset
             */
            position = 0;
            reader.seek(position); // cannot be null here

            // Now we can tailer new lines
            position = readLines(reader);
            last = System.currentTimeMillis();
          }

          // check if file rorated:
          // case1: new file legnth equal old file lengh
          // case2: new file legnth greater than old file length
          if (readCount % 100 == 0) {
            reader = checkFileRotate(length, newer, reader);
          }
        }
        if (reOpen) {
          IOUtils.closeQuietly(reader);
        }
        try {
          Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
        }
        if (run && reOpen) {
          reader = reopen(length, newer, reader);
        }
      }
    } catch (Exception e) {
      listener.handle(e);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * check file rotated
   */
  private RandomAccessFile checkFileRotate(long length, boolean newer, RandomAccessFile reader)
      throws Exception {
    String nowInode = FileUtil.getFileInode(file.getAbsolutePath());
    if (nowInode != null && !nowInode.equals(posEntry.getInode())) {
      logger.info(fileName + ",check file rotated:" + nowInode + "," + posEntry.getInode());
      return rotatedFie(length, newer, reader);
    }
    return reader;
  }

  /**
   * reopen file
   */
  private RandomAccessFile reopen(long length, boolean newer, RandomAccessFile reader) {
    IOUtils.closeQuietly(reader);
    logger.info(fileName + ":file reopen:" + position + "," + length + ","
        + file.lastModified() + "," + file.exists() + "," + newer + "," + listener.getLineCount());
    try {
      reader = new RandomAccessFile(file, RAF_MODE);
      reader.seek(position);
      posEntry.setInode(FileUtil.getFileInode(file.getAbsolutePath()));
    } catch (Exception e) {
      listener.handle(e);
    }
    return reader;
  }

  /**
   * rotatedFie
   */
  private RandomAccessFile rotatedFie(long length, boolean newer, RandomAccessFile reader)
      throws Exception {
    long tmpLine = listener.getLineCount();

    // 等2秒，等上个日志的增量写完
    Thread.sleep(2000);
    String rolledPath = FileUtil
        .getRolledFileWithInode(file.getAbsolutePath(), posEntry.getInode());
    if (StringUtils.isNotBlank(rolledPath)) {
      // 滚动的那个日志文件
      File tmpFile = new File(rolledPath);
      if (tmpFile.length() > position) {
        RandomAccessFile tmpReader = new RandomAccessFile(file, RAF_MODE);
        tmpReader.seek(position);
        readLines(tmpReader);
        IOUtils.closeQuietly(tmpReader);
      }
    }
    logger.info(fileName + ":file rotated:" + position + "," + length + "," +
        file.lastModified() + "," + file.exists() + "," + newer + "," + StringUtils.isNotBlank(
        rolledPath) + "," +
        tmpLine + "," + listener.getLineCount());
    // File was rotated
    listener.fileRotated(position);
    // Reopen the reader after rotation
    try {
      // Ensure that the old file is closed iff we re-open it successfully
      RandomAccessFile save = reader;
      reader = new RandomAccessFile(file, RAF_MODE);
      position = 0;
      // close old file explicitly rather than relying on GC picking up previous RAF
      IOUtils.closeQuietly(save);
    } catch (FileNotFoundException e) {
      // in this case we continue to use the previous reader and position values
      // old file rotate,new file has not open
      listener.fileNotFound();
    }
    posEntry.setInode(FileUtil.getFileInode(file.getAbsolutePath()));
    return reader;
  }

  /**
   * Allows the tailer to complete its current loop and return.
   */
  public void stop() {
    this.run = false;
  }

  private long readLines(final RandomAccessFile reader) throws IOException {
    ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(64);
    long pos = reader.getFilePointer();
    long rePos = pos; // position to re-tailer
    int num;
    boolean seenCR = false;
    while (run && ((num = reader.read(inbuf)) != -1)) {
      for (int i = 0; i < num; i++) {
        // rePos每次读取一行才会改变,如果没有一行这里break也不会有影响
        if (!run) {
          break;
        }
        final byte ch = inbuf[i];
        switch (ch) {
          case '\n':
            seenCR = false; // swallow CR before LF
            rePos = pos + i + 1; //because i start from 0
            String line = new String(lineBuf.toByteArray(), cset);
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
              String line2 = new String(lineBuf.toByteArray(), cset);
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

  public long getPosition() {
    return position;
  }

  public AdvancedTailerListener getListener() {
    return listener;
  }


}
