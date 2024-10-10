package io.resys.hdes.client.spi.util;

/*-
 * #%L
 * wrench-assets-datatype
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class FileUtils {

  private static final String PATH_SEP = "/";
  
  public static String toString(Class<?> type, String resource) {
    try {
      return IOUtils.toString(type.getClassLoader().getResource(resource), StandardCharsets.UTF_8);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public static InputStream toInputStream(Class<?> type, String resource) {
    return IOUtils.toInputStream(toString(type, resource), StandardCharsets.UTF_8);
  }
  
  public static String toString(Object type, String resource) {
    try {
      return IOUtils.toString(type.getClass().getClassLoader().getResource(resource), StandardCharsets.UTF_8);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  

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
