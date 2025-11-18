package io.digiexpress.eveli.client.spi.feedback;

/*-
 * #%L
 * eveli-client
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.digiexpress.eveli.client.api.FeedbackCategoriesReader;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@RequiredArgsConstructor
public class FeedbackCategoriesReaderImpl implements FeedbackCategoriesReader {
  final ObjectMapper mapper;

  public JsonNode getCategories() {
    // categories structure: language -> mainCategory -> subCategory -> list of keywords
    final var resource = new ClassPathResource("assets/categories/categories.json");

    try (final var inputStream = resource.getInputStream()) {
      return mapper.readTree(inputStream);
    } catch (IOException e) {
      throw new RuntimeException("Categories json not found: " + e.getMessage(), e);
    }
  }
}

