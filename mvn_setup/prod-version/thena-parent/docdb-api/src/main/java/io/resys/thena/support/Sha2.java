package io.resys.thena.support;

/*-
 * #%L
 * hdes-object-repo
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import io.resys.thena.api.entities.git.ImmutableBlob;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.git.Blob;
import io.resys.thena.api.entities.git.Commit;
import io.vertx.core.json.JsonObject;

public final class Sha2  {
  private static final TreeEntryComparator comparator = new TreeEntryComparator();

  public static String blobId(JsonObject blob) {
    String id = Hashing
        .murmur3_128()
        .hashString(blob.encode(), Charsets.UTF_8)
        .toString();
    return id;
  }
  public static String treeId(Map<String, TreeValue> values) {
    List<TreeValue> source = new ArrayList<>(values.values());
    Collections.sort(source, comparator);
    String id = Hashing
        .murmur3_128()
        .hashString(source.toString(), Charsets.UTF_8)
        .toString();
    return id;
  }
  
  public static Blob id(Blob blob) {
    String id = Hashing
        .murmur3_128()
        .hashString(blob.getValue().encode(), Charsets.UTF_8)
        .toString();
    return ImmutableBlob.builder().from(blob).id(id).build();
  }

  public static String commitId(Commit commit) {
    String id = Hashing
        .murmur3_128()
        .hashString(commit.toString(), Charsets.UTF_8)
        .toString();
    return id;
  }

  public static String commitId(DocCommit commit) {
    String id = Hashing
        .murmur3_128()
        .hashString(commit.toString(), Charsets.UTF_8)
        .toString();
    return id;
  }
  static class TreeEntryComparator implements Comparator<TreeValue> {
    @Override
    public int compare(TreeValue o1, TreeValue o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }
}
