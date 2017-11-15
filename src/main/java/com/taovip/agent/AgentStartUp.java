package com.taovip.agent;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.taovip.agent.domain.Constants;
import com.taovip.agent.domain.FileEntry;
import com.taovip.agent.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Created by chentao on 16/9/12.
 */
public class AgentStartUp {

  private static final Log logger = LogFactory.getLog(AgentStartUp.class);

  public static void main(String[] args) throws Exception {
    int httport = args.length >= 1 ? Integer.parseInt(args[0]) : Constants.PORT;
    logger.info("going to start");
    List<String> filesConfig = FileUtil.readFileAsLineList(Constants.PULL_LOGS_CONFIG);
    List<FileEntry> fileEntrys = new ArrayList<FileEntry>();
    if (filesConfig != null) {
      for (String config : filesConfig) {
        String[] strs = StringUtils.split(config, ",");
        FileEntry fileEntry = new FileEntry();
        fileEntry.setFilePath(strs[0]);
        if (strs.length > 1) {
          fileEntry.setCharset(strs[1]);
        }
        fileEntrys.add(fileEntry);
      }
    }
    SimpleFileReader.start(fileEntrys, httport);
  }

}
