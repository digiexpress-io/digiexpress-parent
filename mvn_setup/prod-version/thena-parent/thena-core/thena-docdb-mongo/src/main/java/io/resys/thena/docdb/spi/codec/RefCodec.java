package io.resys.thena.docdb.spi.codec;

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

import io.resys.thena.docdb.api.models.ImmutableRef;
import io.resys.thena.docdb.api.models.Objects.Ref;


public class RefCodec implements Codec<Ref> {
  
  public static final String NAME = "_id";
  public static final String COMMIT = "commit";
  
  @Override
  public void encode(BsonWriter writer, Ref command, EncoderContext encoderContext) {
    writer.writeStartDocument();
    writer.writeString(NAME, command.getName());
    writer.writeString(COMMIT, command.getCommit());
    writer.writeEndDocument();
  }

  @Override
  public Ref decode(BsonReader reader, DecoderContext decoderContext) {
    reader.readStartDocument();
    Ref result = ImmutableRef.builder()
      .name(reader.readString(NAME))
      .commit(reader.readString(COMMIT))
      .build();
    reader.readEndDocument();
    return result;
  }

  @Override
  public Class<Ref> getEncoderClass() {
    return Ref.class;
  }
}
