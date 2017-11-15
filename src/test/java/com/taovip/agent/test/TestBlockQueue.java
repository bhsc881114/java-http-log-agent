package com.taovip.agent.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TestBlockQueue {

  @Test
  public void testBlock() {
    LinkedBlockingQueue queue = new LinkedBlockingQueue(10);
    for (int i = 0; i < 12; i++) {
      try {
        System.out.println(queue.offer(i, 1, TimeUnit.SECONDS));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    Assert.assertEquals(10, queue.size());
  }

  /**
   * test drain order as put order
   */
  @Test
  public void testDrain() throws InterruptedException {
    final LinkedBlockingQueue queue = new LinkedBlockingQueue(10000);
    for (int i = 0; i < 10000; i++) {
      try {
        queue.offer(i, 1, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    List result = new ArrayList();
    queue.drainTo(result, 5);
    for (int i = 0; i < 5; i++) {
      Assert.assertEquals(i, result.get(i));
    }

    for (int i = 0; i < 10; i++) {
      new Thread() {
        public void run() {
          while (true) {
            List<Integer> result = new ArrayList<Integer>();
            queue.drainTo(result, 100);
            if (result.size() == 0) {
              break;
            }
            for (int i = 1; i < result.size(); i++) {
              int now = result.get(i);
              int old = result.get(i - 1) + 1;
              if (now != old) {
                System.out.println(StringUtils.join(result, ","));
              }
              Assert.assertEquals(now, old);
            }
            try {
              Thread.sleep(100);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }.start();
    }
    final CountDownLatch cdl = new CountDownLatch(1);
    new Thread() {
      public void run() {
        for (int i = 10000; i < 100000; i++) {
          try {
            queue.offer(i, 1, TimeUnit.SECONDS);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        cdl.countDown();
      }
    }.start();
    cdl.await();
    Thread.sleep(10);
  }
}
