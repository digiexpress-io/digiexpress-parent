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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.digiexpress.eveli.client.api.FeedbackCategoriesReader;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


public class FeedbackCategoriesReaderImpl implements FeedbackCategoriesReader {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public Map<String, Map<String, Map<String, List<String>>>> readCategoriesJsonFile() throws IOException {
    ClassPathResource resource = new ClassPathResource("assets/categories/categories.json");

    try (InputStream inputStream = resource.getInputStream()) {
      return objectMapper.readValue(
        inputStream,
        new TypeReference<Map<String, Map<String, Map<String, List<String>>>>>() {}
      );
    } catch (IOException e) {
      throw new IOException("Failed to read categories.json file", e);
    }
  }
}

