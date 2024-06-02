package io.resys.hdes.client.spi.util;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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
