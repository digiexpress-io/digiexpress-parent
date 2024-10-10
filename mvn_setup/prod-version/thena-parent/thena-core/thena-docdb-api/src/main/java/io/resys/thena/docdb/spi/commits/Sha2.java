package io.resys.thena.docdb.spi.commits;

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

import io.resys.thena.docdb.api.models.ImmutableBlob;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.TreeValue;

public final class Sha2  {
  private static final TreeEntryComparator comparator = new TreeEntryComparator();

  public static String blobId(String blob) {
    String id = Hashing
        .murmur3_128()
        .hashString(blob, Charsets.UTF_8)
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
        .hashString(blob.getValue(), Charsets.UTF_8)
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

  static class TreeEntryComparator implements Comparator<TreeValue> {
    @Override
    public int compare(TreeValue o1, TreeValue o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }
}
