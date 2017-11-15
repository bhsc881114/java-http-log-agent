package com.taovip.agent.test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;
import com.taovip.agent.core.PosFileStoreService;
import com.taovip.agent.domain.Constants;
import com.taovip.agent.domain.FileEntry;
import com.taovip.agent.domain.PosEntry;
import com.taovip.agent.util.JsonUtil;

public class TestPosStore {

  private static String FILE_PATH = "/home/admin/a.log";
  private PosFileStoreService posStore = null;

  @Before
  public void before() {
    new File("/tmp/agent").mkdir();
    for (int i = 0; i < 6; i++) {
      try {
        new File("/tmp/agent/" + i + ".log").createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  public void testV1PosInit() throws Exception {
    initPosFile(true);
    posStore = PosFileStoreService.getINSTANCE();
    posStore.start();

    Map<String, PosEntry> poss = posStore.getPosMapV2();
    System.out.println(JsonUtil.gson.toJson(poss));
    Assert.assertEquals(poss.get("/tmp/agent/1.log").getPos(), 119297047L);
  }


  @Test
  public void testV2PosInit() throws Exception {
    initPosFile(false);
    posStore = PosFileStoreService.getINSTANCE();
    posStore.start();

    Map<String, PosEntry> poss = posStore.getPosMapV2();
    System.out.println(JsonUtil.gson.toJson(poss));
    Assert.assertEquals(poss.get("/tmp/agent/1.log").getPos(), 0);
  }

  @Test
  public void testUpdatePos() throws Exception {
    posStore = PosFileStoreService.getINSTANCE();
    posStore.start();
    FileEntry fileEntry = new FileEntry("/tmp/agent/4.log", "UTF-8");
    posStore.initPosEntry(fileEntry);
    for (int i = 0; i < 1000; i++) {
      posStore.setPos("/tmp/agent/4.log", (System.currentTimeMillis() + i));
      Thread.sleep(200);
    }
  }

  private void initPosFile(boolean isV1) throws Exception {
    File root = new File(Constants.PATH);
    if (!root.exists()) {
      throw new Exception("not exist root path:" + Constants.PATH + ",pls check");
    }
    if (isV1) {
      // delete v2
      new File(Constants.STORE_PATH_V2).delete();
      // delete v1
      File dist = new File(Constants.STORE_PATH_V1);
      if (dist.exists()) {
        dist.delete();
      }
      dist.createNewFile();
      // copy to v1
      File v2File =
          new File(TestPosStore.class.getClassLoader().getSystemResource("position.f").getPath());
      Files.copy(v2File, dist);
    } else {
      // delete v1
      new File(Constants.STORE_PATH_V1).delete();
      // delete v2
      File dist = new File(Constants.STORE_PATH_V2);
      if (dist.exists()) {
        dist.delete();
      }
      dist.createNewFile();
      // copy to v2
      File v2File =
          new File(
              TestPosStore.class.getClassLoader().getSystemResource("position_v2.f").getPath());
      Files.copy(v2File, dist);
    }
  }

  public static void main(String[] args) throws Exception {
    Map<String, PosEntry> posMapV2 = new ConcurrentHashMap<String, PosEntry>();
    PosEntry posEntry1 = new PosEntry();
    posEntry1.setFilePath("/tmp/agent/1.log");
    posMapV2.put(posEntry1.getFilePath(), posEntry1);

    PosEntry posEntry2 = new PosEntry();
    posEntry2.setFilePath("/tmp/agent/2.log");
    posMapV2.put(posEntry2.getFilePath(), posEntry2);

    PosEntry posEntry3 = new PosEntry();
    posEntry3.setFilePath("/tmp/agent/3.log");
    posMapV2.put(posEntry3.getFilePath(), posEntry3);

    String posStr = JsonUtil.gson.toJson(posMapV2);
    System.out.println(posStr);
  }

}
