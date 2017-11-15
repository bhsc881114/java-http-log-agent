package com.taovip.agent.domain;

public class Constants {

  public static final String VERSION = "20171102";
  // public static String CHAR_SET = "UTF-8";
  public static final String PATH = "/home/admin/taovip-agent";
  public static final String PULL_LOGS_CONFIG = PATH + "/pull_files.conf";
  public static final long START_TIME = System.currentTimeMillis();

  // ========================= http constants ====================
  public static final int PORT = 11456;
  public static final int HTTP_PULL_LINE = 1000;
  public static final String HTTP_PATH = "/log2";

  // ========================= tail constants ====================
  public static final int ONE_K = 1024;
  public static final String RAF_MODE = "r";
  public static final int MAX_TAIL_THREADS = 10;
  public static final int PULL_INTERVAL = 1000;

  // ========================= position constants ====================
  public static final long STORE_INTERVAL = 2000;
  public static final String STORE_PATH_V1 = Constants.PATH + "/position.f";
  public static final String STORE_PATH_V2 = Constants.PATH + "/.position_v2.f";
  public static final String STORE_PATH_INODE_CHECK = Constants.PATH + "/.inode_check";
  public static final String STORE_PATH_INODE_CHECK_MV = Constants.PATH + "/.inode_check_mv";

}
