package com.taovip.agent.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.CountDownLatch;

public class WriteUtil {

  public static void write(final String filePath, final int line, boolean append)
      throws InterruptedException {
    CountDownLatch cdl = new CountDownLatch(1);
    write(filePath, cdl, 1000, append);
    cdl.await();
  }

  public static void write(final String filePath, final CountDownLatch cdl, final int line,
      final boolean append) {
    Thread t = new Thread() {
      public void run() {
        try {
          Thread.sleep(5000);
        } catch (Exception e) {
          e.printStackTrace();
        }
        File file = new File(filePath);

        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
          fw = new FileWriter(file, append);
          writer = new BufferedWriter(fw);
          for (int i = 0; i < line; i++) {
            writer.write("line:" + i + "," + System.currentTimeMillis());
            writer.newLine();
            writer.flush();
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          org.apache.commons.io.IOUtils.closeQuietly(writer);
          org.apache.commons.io.IOUtils.closeQuietly(fw);
          try {
            Thread.sleep(5000);
          } catch (Exception e) {
            e.printStackTrace();
          }
          cdl.countDown();
        }
      }
    };
    t.start();
  }

}
