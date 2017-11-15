package com.taovip.agent.file;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import com.taovip.agent.util.FileUtil;
import org.junit.Test;

public class TestINode {

  @Test
  public void testGetINode() throws IOException, InterruptedException {
//    Path path = FileSystems.getDefault().getPath("/Users/www1/logs", "test-back.log");
//    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
//    // sun.nio.fs.UnixFileKey unixFileKey =
//    System.out.println(attrs.fileKey());

    FileUtil.printFileAttr("/Users/www1/logs/test1.log");
  }

}
