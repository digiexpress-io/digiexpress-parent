package io.resys.thena.docdb.spi.codec;

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

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import io.resys.thena.docdb.spi.DocDBEncoder;

public class DocDBEncoderCodec implements DocDBEncoder {
  private final CodecRegistry codecRegistry;

  public DocDBEncoderCodec(CodecRegistry codecRegistry) {
    super();
    this.codecRegistry = codecRegistry;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Bson encode(T object) {
    Codec<T> codec = (Codec<T>) codecRegistry.get(object.getClass());
    
    BsonDocument document = new BsonDocument();
    final var writer = new BsonDocumentWriter(document);
    
    codec.encode(writer, object, EncoderContext.builder().build());
    writer.flush();
    writer.close();
    
    return document;
  }
}
