/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.db.assets.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dialob.api.form.Form;
import io.dialob.api.form.ImmutableForm;
import io.dialob.api.form.ImmutableFormMetadata;

import java.io.IOException;

public class AssetFormDeserializer extends TemplateDeserializer {
  private final ObjectMapper objectMapper;

  public AssetFormDeserializer(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }

  public Form deserialize(ObjectNode input) {
    try {
      JsonNode node = objectMapper.readTree(input.get("content").asText());
      Form form = objectMapper.treeToValue(node, Form.class);
      if(isRevision(input)) {
        return ImmutableForm.builder()
            .from(form)
            .rev(getRevision(input))
            .metadata(
                ImmutableFormMetadata.builder()
                .from(form.getMetadata())
                .putAdditionalProperties(getRevisions(input))
                .putAdditionalProperties(getTags(input))
                .build())
            .build();
      }
      return form;
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
