package io.digiexpress.eveli.textanalyzer.adapter;

/*-
 * #%L
 * eveli-integration-text-analyzer
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


import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.digiexpress.eveli.textanalyzer.adapter.api.SentimentSubcategoryResponse;

class ConverterTest {

  @Test
  void test() throws StreamReadException, DatabindException, UnsupportedEncodingException, IOException {
    String response = """
{
  "sentiment": {
    "id": "1",
    "sentiment": "neutral",
    "confidence": 0.20000000298023224,
    "sentences": [
      {
        "text": "I would like to point out some flaws regarding forest protection.",
        "sentiment": "neutral",
        "scores": {
          "score": -0.2,
          "magnitude": 0.2
        }
      }
    ],
    "timestamp": "2025-09-04T19:16:19.652456Z",
    "modelVersion": "1.0.0",
    "modelId": "gcp_sentiment_analyzer"
  },
  "subcategory": {
    "id": "1",
    "subcategory": "unclassified",
    "confidence": 0.0,
    "matches": [],
    "scores": {},
    "timestamp": "2025-09-04T19:16:19.658994Z",
    "modelVersion": "1.0.0",
    "modelId": "gcp_subcategory_analyzer"
  }
}
        """;
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModules(new JavaTimeModule(), new Jdk8Module());
    SentimentSubcategoryResponse value = mapper.readValue(response.getBytes("UTF-8"), SentimentSubcategoryResponse.class);
    
  }
  @Test
  void test2() throws StreamReadException, DatabindException, UnsupportedEncodingException, IOException {
    String response = """
{
  "sentiment": {
    "id": "1",
    "sentiment": "neutral",
    "confidence": 0.0,
    "sentences": [
      {
        "text": "I would like to point out some flaws regarding forest protection.",
        "sentiment": "neutral",
        "scores": [
          0.0
        ]
      }
    ],
    "timestamp": "2025-09-04T21:24:06.678744Z",
    "modelVersion": "1.0.0",
    "modelId": "gcp_keyword_fallback"
  },
  "subcategory": {
    "id": "1",
    "subcategory": "unclassified",
    "confidence": 0.0,
    "matches": [],
    "scores": {},
    "timestamp": "2025-09-04T21:24:06.680105Z",
    "modelVersion": "1.0.0",
    "modelId": "gcp_subcategory_analyzer"
  }
}
        """;
    
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModules(new JavaTimeModule(), new Jdk8Module());
    SentimentSubcategoryResponse value = mapper.readValue(response.getBytes("UTF-8"), SentimentSubcategoryResponse.class);
    
  }

}
