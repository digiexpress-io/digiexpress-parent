package io.digiexpress.eveli.textanalyzer.adapter.spi;

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

import java.util.ArrayList;

import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.digiexpress.eveli.textanalyzer.adapter.api.FeedbackAnalyzerClient;
import io.digiexpress.eveli.textanalyzer.adapter.api.SentimentSubcategoryRequest;
import io.digiexpress.eveli.textanalyzer.adapter.api.SentimentSubcategoryResponse;
import io.digiexpress.eveli.textanalyzer.adapter.api.SimilarityRequest;
import io.digiexpress.eveli.textanalyzer.adapter.api.SimilarityResponse;
import io.digiexpress.eveli.textanalyzer.properties.AnalyzerServerProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reference implementation for feedback analyzer service adapter.
 * This implementation assumes that feedback analyzer service has base URL defined in @see {@link AnalyzerServerProps} property 
 * with 2 endpoints:
 * <ul>
 * <li>/find-similar - to find similar texts
 * <li>/find-sentiment-and-subcategory - to determine given text's sentiment and possible subcategory
 * </ul>
 */
@RequiredArgsConstructor
@Slf4j
public class FeedbackAnalyzerRestClient implements FeedbackAnalyzerClient{

  private final AnalyzerServerProps properties;
  private final ObjectMapper mapper;

  @Override
  public SimilarityResponse findSimilar(SimilarityRequest request) {
    return executeCall(request, "find-similar", SimilarityResponse.class);
  }

  @Override
  public SentimentSubcategoryResponse findSentimentAndSubcategory(SentimentSubcategoryRequest request) {
    return executeCall(request, "find-sentiment-and-subcategory", SentimentSubcategoryResponse.class);
  }
  
  private RestClient getRestClient() {
    // this factory is needed to pass request size to analyzer, by default it is not sent and 
    // analyzer does not recognize body, resulting in error.
    // See https://github.com/spring-projects/spring-framework/issues/31854#issuecomment-1858817976
    var factory = new  BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    return RestClient.builder()
        .requestFactory(factory).build();
  }

  
  private <R, B> R executeCall(B body, String path, Class<R> clazz) {
    if (log.isDebugEnabled()) {
      try {
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(body);
        log.debug("Analyzer call to path {}, body: {}", String.format("%s/%s", properties.getEndpointUrl(), path), json);
      }
      catch (Exception e) {
        log.warn("Body json formatting failed", e);
      }
    }
    RestClient client = getRestClient();
    R result = client.post().uri(String.format("%s/%s", properties.getEndpointUrl(), path))
    .contentType(MediaType.APPLICATION_JSON)
    .body(body)
    .retrieve()
    .onStatus(code -> code.is4xxClientError(), (request, response) -> {
      HttpValidationError restErrorResponse = null;
      try {
        restErrorResponse = mapper.readValue(response.getBody(), HttpValidationError.class);
      }
      catch (Exception e) {
        String bodyValue = mapper.readValue(response.getBody(), String.class);
        log.error("Error response parsing failed, body: {}", bodyValue, e);
        restErrorResponse = new HttpValidationError();
        restErrorResponse.setDetail(new ArrayList<>());
        HttpValidationError.Item item = new HttpValidationError.Item();
        item.setMsg("Error in parsing response:" + response.getStatusText());
        item.setType("Error");
        restErrorResponse.getDetail().add(item);
      }
      log.warn("Analyzer call failed with error: {}", restErrorResponse);
      throw new AnalyzerApiCallException("AnalyzerCallFailed", restErrorResponse, response.getStatusCode()); 
    })
    .body(clazz);
    return result;
  }



}
