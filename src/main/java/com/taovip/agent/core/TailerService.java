package com.taovip.agent.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taovip.agent.domain.Constants;
import com.taovip.agent.domain.FileEntry;
import com.taovip.agent.domain.LineEntry;
import com.taovip.agent.domain.PosEntry;
import com.taovip.agent.tailer.AdvancedTailer;
import com.taovip.agent.tailer.DefaultQueueTailerListener;

/**
 * @author chentao
 */
public class TailerService extends BaseService {

  private static final Log logger = LogFactory.getLog(TailerService.class);

  private ExecutorService executor = null;
  private PosFileStoreService posStoreService = PosFileStoreService.getINSTANCE();
  private ConcurrentHashMap<String, AdvancedTailer> fileTailerMap =
      new ConcurrentHashMap<String, AdvancedTailer>();

  private TailerService() {
  }

  public static TailerService getINSTANCE() {
    return INSTANCE;
  }

  private static final TailerService INSTANCE = new TailerService();

  protected void start0() throws Exception {
    posStoreService.start();
    executor = Executors.newFixedThreadPool(Constants.MAX_TAIL_THREADS);

    Map<String, PosEntry> mapEntry = posStoreService.getPosMapV2();
    if (mapEntry != null && mapEntry.size() > 0) {
      for (Map.Entry<String, PosEntry> posEntry : mapEntry.entrySet()) {
        if (!fileTailerMap.containsKey(posEntry.getKey())) {
          FileEntry fileEntry =
              new FileEntry(posEntry.getValue().getFilePath(), posEntry.getValue().getCharset());
          addFile(fileEntry);
        }
      }
    }
  }

  public boolean addFile(FileEntry fileEntry) {
    if (StringUtils.isBlank(fileEntry.getFilePath())) {
      return false;
    }
    if (fileTailerMap.size() + 1 > Constants.MAX_TAIL_THREADS) {
      logger.warn("can't support more than ten file >< :" + fileEntry.getFilePath());
      return false;
    }
    AdvancedTailer tailer = fileTailerMap.get(fileEntry.getFilePath());
    if (tailer != null) {
      logger.warn("file:" + fileEntry.getFilePath() + " is tailing");
      return false;
    }
    startTailFile(fileEntry);
    return true;
  }

  public boolean removeFile(String filePath) {
    if (StringUtils.isBlank(filePath)) {
      return false;
    }
    AdvancedTailer tailer = fileTailerMap.get(filePath);
    if (tailer == null) {
      logger.warn("file:" + filePath + " not tailing");
      return false;
    }
    tailer.stop();
    fileTailerMap.remove(filePath);
    posStoreService.remove(filePath);
    return true;
  }

  private void startTailFile(FileEntry fileEntry) {
    File file = new File(fileEntry.getFilePath());
    PosEntry posEntry = posStoreService.initPosEntry(fileEntry);
    AdvancedTailer tailer =
        new AdvancedTailer(file, posEntry, new DefaultQueueTailerListener(file),
            Constants.PULL_INTERVAL, false,
            100 * Constants.ONE_K);
    AdvancedTailer preTailer = fileTailerMap.putIfAbsent(file.getAbsolutePath(), tailer);
    if (preTailer == null) {
      executor.execute(tailer);
    }
  }

  public Map<String, List<LineEntry>> pullAllLogs() {
    Map<String, List<LineEntry>> fileLines = new HashMap<String, List<LineEntry>>();

    for (Map.Entry<String, AdvancedTailer> entry : fileTailerMap.entrySet()) {
      List<LineEntry> tmpLines = new ArrayList<LineEntry>(Constants.HTTP_PULL_LINE);
      entry.getValue().getListener().pull(tmpLines, Constants.HTTP_PULL_LINE);

      if (tmpLines.size() > 0) {
        fileLines.put(entry.getKey(), tmpLines);
      }
    }
    return fileLines;
  }

  public Map<String, List<LineEntry>> pullWithLogNames(String[] paths) {
    Map<String, List<LineEntry>> fileLines = new HashMap<String, List<LineEntry>>();
    if (paths == null) {
      return fileLines;
    }
    for (String path : paths) {
      AdvancedTailer tailer = fileTailerMap.get(path);
      if (tailer == null) {
        logger.info("path:" + path + " not tail,please check");
        continue;
      }
      List<LineEntry> tmpLines = new ArrayList<LineEntry>(Constants.HTTP_PULL_LINE);
      tailer.getListener().pull(tmpLines, Constants.HTTP_PULL_LINE);

      if (tmpLines.size() > 0) {
        fileLines.put(path, tmpLines);
      }
    }
    return fileLines;
  }

  public void setPos(String filePath, long pos) {
    posStoreService.setPos(filePath, pos);
  }

  public String getTailFileList() {
    StringBuffer sb = new StringBuffer();
    for (Map.Entry<String, AdvancedTailer> entry : fileTailerMap.entrySet()) {
      sb.append(entry.getKey()).append(";");
    }
    return sb.toString();
  }

  public String getTailInfo() {
    StringBuffer sb = new StringBuffer();
    for (Map.Entry<String, AdvancedTailer> entry : fileTailerMap.entrySet()) {
      String path = entry.getKey();
      AdvancedTailer tailer = entry.getValue();
      PosEntry posEntry = posStoreService.getPosMapV2().get(path);
      sb.append("tailerInfo:").append(path).append(",pull position:").append(tailer.getPosition())
          .append(",pull line:").append(tailer.getListener().getLineCount()).append(",inode:")
          .append(posEntry.getInode()).append(",persistent position:").append(posEntry.getPos())
          .append(",lastReadTime:").append(posEntry.getLastReadtime()).append("\n");
    }
    return sb.toString();
  }

  protected void stop0() {
    for (Map.Entry<String, AdvancedTailer> entry : fileTailerMap.entrySet()) {
      try {
        entry.getValue().stop();
        logger.info("stop path:" + entry.getKey() + ",line:"
            + entry.getValue().getListener().getLineCount() + ",queueSize:"
            + entry.getValue().getListener().getQueueSize());
      } catch (Exception e) {
        logger.warn("stop tailer exception", e);
      }
    }
    executor.shutdownNow();
    posStoreService.stop();
  }

  public ConcurrentHashMap<String, AdvancedTailer> getFileTailerMap() {
    return fileTailerMap;
  }

  public PosFileStoreService getPosStoreService() {
    return posStoreService;
  }


}
