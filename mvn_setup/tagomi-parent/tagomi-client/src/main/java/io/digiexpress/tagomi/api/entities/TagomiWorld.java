package io.digiexpress.tagomi.api.entities;

/*-
 * #%L
 * tagomi-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

// world state after compilation 
public interface TagomiWorld {
  Map<String, List<TagomiProgram>> getProgramsByName();

  interface PdfCompiler {
    PdfCompiler inputField(String name, Serializable value);
    PdfCompiler inputMap(Map<String, Serializable> input);
    PdfCompiler inputEntity(Object inputObject);
    PdfCompiler inputList(List<Object> inputObject);
    PdfCompiler inputJson(JsonNode json);
    PdfCompiler locale(String locale);
    
    Uni<PdfEnvelope> compile(String programIdOrName);
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutablePdfEnvelope.class)
  @JsonDeserialize(as = ImmutablePdfEnvelope.class)
  interface PdfEnvelope {
    TagomiPdfStatus getStatus();
    @Nullable Pdf getValue();
  }
  
  interface Pdf {
    String getName();
    String getLocale();
    byte[] getBody();
  }
  
  enum TagomiPdfStatus {
    OK, ERROR
  }
}
