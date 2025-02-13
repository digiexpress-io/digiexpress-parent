package io.dialob.client.spi.support;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

public class Sha2 {
  
  
  public static String blobId(String path, String rev) {
    String id = Hashing
        .murmur3_128()
        .hashString(path + "/" + rev, Charsets.UTF_8)
        .toString();
    return id;
  }
  
  public static String blob(String value) {
    String id = Hashing
        .murmur3_128()
        .hashString(value, Charsets.UTF_8)
        .toString();
    return id;
  }
}
