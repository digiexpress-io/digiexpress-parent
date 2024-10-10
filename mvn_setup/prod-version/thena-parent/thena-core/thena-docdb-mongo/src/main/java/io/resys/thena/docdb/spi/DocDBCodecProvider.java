package io.resys.thena.docdb.spi;

/*-
 * #%L
 * thena-docdb-mongo
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.api.models.Objects.Ref;
import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.spi.codec.BlobCodec;
import io.resys.thena.docdb.spi.codec.CommitCodec;
import io.resys.thena.docdb.spi.codec.RefCodec;
import io.resys.thena.docdb.spi.codec.RepoCodec;
import io.resys.thena.docdb.spi.codec.TagCodec;
import io.resys.thena.docdb.spi.codec.TreeCodec;
import io.resys.thena.docdb.spi.codec.TreeEntryCodec;


public class DocDBCodecProvider implements CodecProvider {

  private final CommitCodec commit = new CommitCodec();
  private final BlobCodec blob = new BlobCodec();
  private final TreeEntryCodec treeEntry = new TreeEntryCodec();
  private final TreeCodec tree = new TreeCodec(treeEntry);
  private final TagCodec tag = new TagCodec();
  private final RefCodec ref = new RefCodec();
  private final RepoCodec repo = new RepoCodec();
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry)  {
    if(Repo.class.isAssignableFrom(clazz)) {
      return (Codec<T>) repo;
    }
    if(Commit.class.isAssignableFrom(clazz)) {
      return (Codec<T>) commit;
    }
    if(Blob.class.isAssignableFrom(clazz)) {
      return (Codec<T>) blob;
    }
    if(Tree.class.isAssignableFrom(clazz)) {
      return (Codec<T>) tree;
    }
    if(TreeValue.class.isAssignableFrom(clazz)) {
      return (Codec<T>) treeEntry;
    }
    if(Tag.class.isAssignableFrom(clazz)) {
      return (Codec<T>) tag;
    }
    if(Ref.class.isAssignableFrom(clazz)) {
      return (Codec<T>) ref;
    }
    return null;
  }
}
