package com.taovip.agent.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.taovip.agent.domain.Constants;

public class FileUtil {

  private static Log logger = LogFactory.getLog(FileUtil.class);
  private static Cache<String, String> fileInodeCache = CacheBuilder.newBuilder()
      .maximumSize(3000).expireAfterAccess(2, TimeUnit.DAYS).build();

  /**
   * get file inode
   */
  public static String getFileInode(String filePath) {
    try {
      Path path = Paths.get(filePath);
      BasicFileAttributes bfa = Files.readAttributes(path, BasicFileAttributes.class);

      String s = bfa.fileKey().toString();
      String inode = s.substring(s.indexOf("ino=") + 4, s.indexOf(")"));
      return inode;
    } catch (Exception e) {
      logger.warn("", e);
    }
    return null;
  }

  /**
   *
   * @param fileName
   * @return
   */
  public static List<String> readFileAsLineList(String fileName) {
    FileReader reader = null;
    BufferedReader br = null;
    List<String> list = new ArrayList<String>();
    try {
      reader = new FileReader(fileName);
      br = new BufferedReader(reader);

      String str = null;
      while ((str = br.readLine()) != null) {
        list.add(str);
      }
    } catch (Exception e) {

    } finally {
      IOUtils.closeQuietly(br);
      IOUtils.closeQuietly(reader);
    }
    return list;
  }

  /**
   *
   * @param fileName
   * @return
   */
  public static String readFileAsString(String fileName) {
    String encoding = "UTF-8";
    File file = new File(fileName);
    Long filelength = file.length();
    byte[] filecontent = new byte[filelength.intValue()];
    try {
      FileInputStream in = new FileInputStream(file);
      in.read(filecontent);
      in.close();
    } catch (Exception e) {
      logger.warn(e);
    }
    try {
      return new String(filecontent, encoding);
    } catch (UnsupportedEncodingException e) {
      logger.warn(e);
      return null;
    }
  }

  /**
   * 找到inode匹配的那个文件
   */
  public static String getRolledFileWithInode(String fileName, String inode) {
    File file = new File(fileName);
    File folder = file.getParentFile();
    if (!folder.isDirectory()) {
      logger.warn("error path:" + folder.getAbsolutePath() + " is not folder");
    }
    File[] listFile = FileUtil.listFile(folder);
    for (File child : listFile) {
      String tpath = child.getAbsolutePath();
      if (fileName.equals(tpath)) {
        continue;
      }
      String inodeCache = fileInodeCache.getIfPresent(tpath);
      try {
        if (inodeCache == null) {
          inodeCache = FileUtil.getFileInode(tpath);

          if (inodeCache != null) {
            fileInodeCache.put(tpath, inodeCache);
          }
        }
        if (inode != null && inode.equals(inodeCache)) {
          return tpath;
        }
      } catch (Exception e) {
        logger.warn("get inode path:" + tpath + "," + inodeCache + " exception", e);
      }
    }
    return null;
  }

  public static File[] listFile(File folder) {
    return folder.listFiles();
  }

  public static boolean fileExist(String path) {
    return new File(path).exists();
  }

  public static void main(String[] args) throws Exception {
    // printFileAttr(args[0]);
    printFileAttr("/tmp/agent/1.log");
  }

  public static void fileInodeCheck() throws Exception {
    File file = new File(Constants.STORE_PATH_INODE_CHECK);
    File fileMv = new File(Constants.STORE_PATH_INODE_CHECK_MV);

    file.delete();
    fileMv.delete();
    file.createNewFile();

    String inode = getFileInode(Constants.STORE_PATH_INODE_CHECK);
    file.renameTo(fileMv);
    String inodeMv = getFileInode(Constants.STORE_PATH_INODE_CHECK_MV);
    if (!inode.equals(inodeMv)) {
      throw new Exception("not support inode!agent can't work correct");
    }
  }

  public static void printFileAttr(String filePath) throws IOException, InterruptedException {
    Path path = Paths.get(filePath);

    BasicFileAttributes bfa = Files.readAttributes(path, BasicFileAttributes.class);
    System.out.println("Creation Time      : " + bfa.creationTime());
    System.out.println("Last Access Time   : " + bfa.lastAccessTime());
    System.out.println("Last Modified Time : " + bfa.lastModifiedTime());
    System.out.println("Is Directory       : " + bfa.isDirectory());
    System.out.println("Is Other           : " + bfa.isOther());
    System.out.println("Is Regular File    : " + bfa.isRegularFile());
    System.out.println("Is Symbolic Link   : " + bfa.isSymbolicLink());
    System.out.println("Size               : " + bfa.size());

    Object objectKey = bfa.fileKey();
    String s = objectKey.toString();
    String inode = s.substring(s.indexOf("ino=") + 4, s.indexOf(")"));
    System.out.println("Object Key         : " + bfa.fileKey());
    System.out.println("Object Key type    : " + objectKey.getClass());
    System.out.println(inode);
  }

}
