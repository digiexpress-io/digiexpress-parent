package io.resys.thena.docdb.spi.codec;

/*-
 * #%L
 * hdes-object-repo-mongodb
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

import java.util.HashMap;
import java.util.Map;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import io.resys.thena.docdb.api.models.ImmutableTree;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Objects.TreeValue;

public class TreeCodec implements Codec<Tree> {
  
  public static final String ID = "_id";
  public static final String VALUES = "values";
  
  private final TreeEntryCodec entryCode;
  
  public TreeCodec(TreeEntryCodec entryCode) {
    super();
    this.entryCode = entryCode;
  }

  @Override
  public void encode(BsonWriter writer, Tree value, EncoderContext encoderContext) {
    writer.writeStartDocument();
    writer.writeString(ID, value.getId());
    writer.writeStartArray(VALUES);
    
    for (TreeValue command : value.getValues().values()) {
      entryCode.encode(writer, command, encoderContext);
    }
    writer.writeEndArray();
    writer.writeEndDocument();
  }

  @Override
  public Tree decode(BsonReader reader, DecoderContext decoderContext) {
    ImmutableTree.Builder changes = ImmutableTree.builder();
    
    reader.readStartDocument();
    changes.id(reader.readString(ID));
    
    reader.readName(VALUES);
    reader.readStartArray();
    Map<String, TreeValue> commands = new HashMap<>();
    
    while(reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      TreeValue entry = entryCode.decode(reader, decoderContext);
      commands.put(entry.getName(), entry);
    }
    reader.readEndArray();
    
    reader.readEndDocument();
    return changes.values(commands).build();
  }
  
  @Override
  public Class<Tree> getEncoderClass() {
    return Tree.class;
  }
}
