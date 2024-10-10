package io.resys.thena.docdb.spi.codec;

import java.time.LocalDateTime;
import java.util.Optional;

/*-
 * #%L
 * hdes-storage-mongodb
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

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import io.resys.thena.docdb.api.models.ImmutableCommit;
import io.resys.thena.docdb.api.models.Objects.Commit;


public class CommitCodec implements Codec<Commit> {
  
  public static final String ID = "_id";
  private static final String DATE_TIME = "dateTime";
  private static final String AUTHOR = "author";
  private static final String MESSAGE = "message";
  private static final String TREE = "tree";
  private static final String PARENT = "parent";
  private static final String MERGE = "merge";
  
  @Override
  public void encode(BsonWriter writer, Commit command, EncoderContext encoderContext) {
    writer.writeStartDocument();
    writer.writeString(ID, command.getId());
    writer.writeString(DATE_TIME, command.getDateTime().toString());
    writer.writeString(AUTHOR, command.getAuthor());
    writer.writeString(MESSAGE, command.getMessage());
    writer.writeString(TREE, command.getTree());
    
    if (command.getParent().isPresent()) {
      writer.writeString(PARENT, command.getParent().get());
    } else {
      writer.writeNull(PARENT);
    }
    
    if (command.getMerge().isPresent()) {
      writer.writeString(MERGE, command.getMerge().get());
    } else {
      writer.writeNull(MERGE);
    }
    
    writer.writeEndDocument();
  }

  @Override
  public Commit decode(BsonReader reader, DecoderContext decoderContext) {
    reader.readStartDocument();
    Commit result = ImmutableCommit.builder()
      .id(reader.readString(ID))
      .dateTime(LocalDateTime.parse(reader.readString(DATE_TIME)))
      .author(reader.readString(AUTHOR))
      .message(reader.readString(MESSAGE))
      .tree(reader.readString(TREE))
      .parent(Optional.ofNullable(isNull(PARENT, reader) ? null : reader.readString()))
      .merge(Optional.ofNullable(isNull(MERGE, reader) ? null : reader.readString()))
      .build();
    reader.readEndDocument();
    return result;
  }

  private boolean isNull(String id, BsonReader reader) {
    reader.readName(id);
    if (reader.getCurrentBsonType() == BsonType.NULL) {
      reader.readNull();
      return true;
    }
    return false;
  }

  @Override
  public Class<Commit> getEncoderClass() {
    return Commit.class;
  }
}
