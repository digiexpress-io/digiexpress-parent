package io.digiexpress.spring.composer.controllers.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileUtils {

  private static final String PATH_SEP = "/";


  public static List<String> splitPath(String path) {
    String cleanPath = cleanPath(path);
    if(cleanPath != null && !cleanPath.isEmpty()) {
      return Arrays.asList(cleanPath.split(PATH_SEP));
    }
    return Collections.emptyList();
  }

  public static String cleanPath(String path) {
    return cleanPathStart(cleanPathEnd(path));
  }

  public static String cleanPathStart(String path) {
    if(path.length() == 0) {
      return path;
    }
    if(path.startsWith(PATH_SEP)){
      return cleanPathStart(path.substring(1));
    } else {
      return path;
    }
  }

  public static String cleanPathEnd(String path) {
    if(path.length() == 0) {
      return path;
    }
    if(path.endsWith(PATH_SEP)){
      return cleanPathEnd(path.substring(0, path.length() -1));
    } else {
      return path;
    }
  }
}