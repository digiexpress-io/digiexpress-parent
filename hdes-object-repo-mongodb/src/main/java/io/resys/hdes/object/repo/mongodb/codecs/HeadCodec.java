package io.resys.hdes.object.repo.mongodb.codecs;

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
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import io.resys.hdes.object.repo.api.ImmutableHead;
import io.resys.hdes.object.repo.api.ObjectRepository.Head;


public class HeadCodec implements Codec<Head> {
  
  public static final String ID = "_id";
  public static final String COMMIT = "commit";
  
  @Override
  public void encode(BsonWriter writer, Head command, EncoderContext encoderContext) {
    writer.writeStartDocument();
    writer.writeString(ID, command.getName());
    writer.writeString(COMMIT, command.getCommit());
    writer.writeEndDocument();
  }

  @Override
  public Head decode(BsonReader reader, DecoderContext decoderContext) {
    reader.readStartDocument();
    Head result = ImmutableHead.builder()
      .name(reader.readString(ID))
      .commit(reader.readString(COMMIT))
      .build();
    reader.readEndDocument();
    return result;
  }

  @Override
  public Class<Head> getEncoderClass() {
    return Head.class;
  }
}
