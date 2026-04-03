package io.resys.thena.fs.jackson;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.ImmutableTree;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.jackson.NodesAndBlobStdDeserializer.NodesAndBlob;
import io.resys.thena.jackson.QuarkusJacksonJsonCodec;
import lombok.Builder;

public class NodesAndBlobStdDeserializer extends StdDeserializer<NodesAndBlob> {

  private static final long serialVersionUID = 7671659008272389681L;

  @Builder
  public record NodesAndBlob (
    Map<UUID, Blob> blobsById,
    Map<UUID, Props> propsById,
    Map<String, Node> trees_nodes_by_objectId,
    Map<UUID, Node> trees_nodes_by_uuid,
    Map<String, Node> tree_nodes_by_path
  ) {
    public ImmutableTree.Builder toTreeBuilder() {
      return new ImmutableTree.Builder(trees_nodes_by_objectId, trees_nodes_by_uuid, tree_nodes_by_path);
    }
  }
  public NodesAndBlobStdDeserializer() {
    this(NodesAndBlob.class);
  }
  public NodesAndBlobStdDeserializer(Class<?> vc) {
    super(vc);
  }
  @Override
  public NodesAndBlob deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
    if (p.currentToken() != JsonToken.START_ARRAY) {
      throw new IOException("Expected array");
    }
    final var nodes = new NodesAndBlobParser(p).parse();
    
    return nodes;
  }
  public static NodesAndBlob deserialize(String text) {
    if(text == null) {
      return null;
    }
    try (final var parser = QuarkusJacksonJsonCodec.createParser(text)) {
      parser.nextToken(); // Move to START_ARRAY
      return new NodesAndBlobStdDeserializer().deserialize(parser, null);
    } catch(Exception e) {
      throw new RuntimeException("Failed to deserialize nodes and blobs because of error: " + e.getMessage(), e);
    }
  }
}
