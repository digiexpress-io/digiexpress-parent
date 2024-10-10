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

import io.resys.thena.docdb.api.models.ImmutableBlob;
import io.resys.thena.docdb.api.models.Objects.Blob;


public class BlobCodec implements Codec<Blob> {
  
  public static final String ID = "_id";
  public static final String VALUE = "value";
  
  @Override
  public void encode(BsonWriter writer, Blob command, EncoderContext encoderContext) {
    writer.writeStartDocument();
    writer.writeString(ID, command.getId());
    writer.writeString(VALUE, command.getValue());
    writer.writeEndDocument();
  }

  @Override
  public Blob decode(BsonReader reader, DecoderContext decoderContext) {
    reader.readStartDocument();
    Blob result = ImmutableBlob.builder()
      .id(reader.readString(ID))
      .value(reader.readString(VALUE))
      .build();
    reader.readEndDocument();
    return result;
  }

  @Override
  public Class<Blob> getEncoderClass() {
    return Blob.class;
  }
}
