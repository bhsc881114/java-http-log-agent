package com.taovip.agent.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.reflect.TypeToken;
import com.taovip.agent.domain.Constants;
import com.taovip.agent.domain.FileEntry;
import com.taovip.agent.domain.PosEntry;
import com.taovip.agent.util.FileUtil;
import com.taovip.agent.util.JsonUtil;

/**
 * position store in file
 *
 * @author chentao
 */
public class PosFileStoreService extends BaseService {

  private static final Log logger = LogFactory.getLog(PosFileStoreService.class);

  private TailerService tailerService = null;
  private Map<String, PosEntry> posMapV2 = new ConcurrentHashMap<String, PosEntry>();

  private final static PosFileStoreService INSTANCE = new PosFileStoreService();

  public static PosFileStoreService getINSTANCE() {
    return INSTANCE;
  }

  private PosFileStoreService() {
  }

  /**
   * start position store service
   */
  protected void start0() throws Exception {
    tailerService = TailerService.getINSTANCE();
    posMapV2 = initStoredPosData();

    new Thread() {
      public void run() {
        while (RUNNING) {
          try {
            Thread.sleep(Constants.STORE_INTERVAL);
            if (RUNNING) {
              flushPosData();
            }
          } catch (Exception e) {
            logger.warn(e);
          }
        }
      }
    }.start();
  }

  /**
   * stop position store service
   */
  protected void stop0() {
    flushPosData();
  }

  /**
   * set file position
   */
  public void setPos(String filePath, long pos) {
    PosEntry entry = posMapV2.get(filePath);
    if (entry != null) {
      entry.setPos(pos);
    }
  }

  public void remove(String filePath) {
    posMapV2.remove(filePath);
  }

  /**
   * init file position
   */
  public PosEntry initPosEntry(FileEntry fileEntry) {
    PosEntry entry = posMapV2.get(fileEntry.getFilePath());
    if (entry == null) {
      entry = new PosEntry();
      entry.setFilePath(fileEntry.getFilePath());
      entry.setPos(0);
      entry.setCharset(fileEntry.getCharset());

      String inode = FileUtil.getFileInode(fileEntry.getFilePath());
      entry.setInode(inode);
      posMapV2.put(fileEntry.getFilePath(), entry);
    }
    return entry;
  }

  /**
   * get position
   */
  private Map<String, PosEntry> initStoredPosData() throws Exception {
    Map<String, PosEntry> posMapV2 = new HashMap<String, PosEntry>();
    boolean v2Exist = FileUtil.fileExist(Constants.STORE_PATH_V2);
    String path = v2Exist ? Constants.STORE_PATH_V2 : Constants.STORE_PATH_V1;
    String cn = FileUtil.readFileAsString(path);

    if (StringUtils.isNotBlank(cn)) {
      logger.info("pos file:" + v2Exist + ",constant:" + cn);
      if (!v2Exist) {
        Map<String, Long> posMap = JsonUtil.gson.fromJson(cn, new TypeToken<Map<String, Long>>() {
        }.getType());
        for (Map.Entry<String, Long> entry : posMap.entrySet()) {
          PosEntry posEntry = new PosEntry();
          posEntry.setPos(entry.getValue());
          posEntry.setFilePath(entry.getKey());

          String inode = FileUtil.getFileInode(entry.getKey());// set inode
          posEntry.setInode(inode);
          posMapV2.put(entry.getKey(), posEntry);
        }
      } else {
        posMapV2 = JsonUtil.gson.fromJson(cn, new TypeToken<Map<String, PosEntry>>() {
        }.getType());

        for (Map.Entry<String, PosEntry> entry : posMapV2.entrySet()) {
          try {
            String newInode = FileUtil.getFileInode(entry.getKey());
            // file has change
            if (newInode != null && !newInode.equals(entry.getValue().getInode())) {
              logger.info(
                  "file rolled:" + entry.getKey() + ",old inode:" + entry.getValue().getInode()
                      + ",new inode:" +
                      newInode + ",old lastReadTime:" + entry.getValue().getLastReadtime());
              entry.getValue().setInode(newInode);
              entry.getValue().setPos(0);
            }
          } catch (Exception e) {
            logger.info("", e);
          }
        }
      }
    }
    return posMapV2;
  }

  private synchronized void flushPosData() {
    BufferedWriter out = null;
    try {
      boolean flush = (posMapV2.size() != 0 ||
          (posMapV2.size() == 0 && System.currentTimeMillis() - Constants.START_TIME > 60 * 1000));
      if (flush) {
        out = new BufferedWriter(new FileWriter(Constants.STORE_PATH_V2));
        String posStr = JsonUtil.gson.toJson(posMapV2);
        out.write(posStr);

        logger.info(tailerService.getTailInfo());
      }
    } catch (IOException e) {
      logger.warn(e);
    } finally {
      if (out != null) {
        org.apache.commons.io.IOUtils.closeQuietly(out);
      }
    }
  }

  public Map<String, PosEntry> getPosMapV2() {
    return posMapV2;
  }
}

