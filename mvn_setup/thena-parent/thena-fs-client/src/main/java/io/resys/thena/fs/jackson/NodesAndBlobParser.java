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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.jackson.NodesAndBlobStdDeserializer.NodesAndBlob;



public class NodesAndBlobParser {
  private final JsonParser root;
  private final Map<UUID, Blob> blobsById = new HashMap<>();
  private final Map<UUID, Props> propsById = new HashMap<>();
  private final Map<String, Node> trees_nodes_by_objectId = new HashMap<>();
  private final Map<UUID, Node> trees_nodes_by_uuid = new HashMap<>();
  private final Map<String, Node> tree_nodes_by_path = new HashMap<>();
  
  private final List<UUID> visited_blobs = new ArrayList<>();
  private final List<UUID> visited_props = new ArrayList<>();
  
  public NodesAndBlobParser(JsonParser root) throws IOException {
    super();
    this.root = root;
    if(root.currentToken() != JsonToken.START_ARRAY) {
      throw new IOException("Expected array");
    }
  }
  
  public NodesAndBlob parse() throws IOException {
    // Loop through array elements
    while (root.nextToken() != JsonToken.END_ARRAY) {
      if (root.currentToken() == JsonToken.START_OBJECT) {

        final Node node = new NodeParser(root).parse();

        // Populate the Maps
        trees_nodes_by_objectId.put(node.getObjectId(), node);
        trees_nodes_by_uuid.put(node.getId(), node);

        // Build path from nodePath + nodeName
        final String path = node.getNodePath().map(p -> p + "/" + node.getNodeName()).orElse(node.getNodeName());
        tree_nodes_by_path.put(path, node);

        // Add blob and props if present
        if (node.getTransitives().getBlob() != null && !visited_blobs.contains(node.getBlobId().get())) {
          blobsById.put(node.getBlobId().get(), node.getTransitives().getBlob());
          visited_blobs.add(node.getBlobId().get());
        }
        if (node.getTransitives().getProps() != null && !visited_props.contains(node.getPropsId().get())) {
          propsById.put(node.getPropsId().get(), node.getTransitives().getProps());
          visited_props.add(node.getPropsId().get());
        }
      }
    }
    
    return NodesAndBlob.builder()
      .blobsById(blobsById)
      .propsById(propsById)
      
      .tree_nodes_by_path(tree_nodes_by_path)
      .trees_nodes_by_objectId(trees_nodes_by_objectId)
      .trees_nodes_by_uuid(trees_nodes_by_uuid)
      
      .build();
  }
}
