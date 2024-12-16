package io.digiexpress.eveli.dialob.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*-
 * #%L
 * dialob-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.FormTag;
import io.dialob.api.form.FormTag.Type;
import io.dialob.api.form.FormValueSet;
import io.dialob.api.form.FormValueSetEntry;
import io.dialob.api.form.ImmutableFormTag;
import io.dialob.api.proto.ImmutableValueSet;
import io.dialob.api.proto.ImmutableValueSetEntry;
import io.dialob.api.proto.ValueSet;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.ImmutableQuestionnaire;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.dialob.api.DialobProxy;
import io.digiexpress.eveli.dialob.spi.DialobAssert.DialobException;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
public class DialobClientImpl implements DialobClient {
  
  private final ObjectMapper objectMapper;
  private final DialobService dialobService;
  
  private static String LOOKUP = "__MOD_";
  
  @Override
  public ProxyAnswer proxyAnswer(Questionnaire q, Answer answer) {
    final var formItem = DialobClientImpl.LOOKUP(answer, q, objectMapper);
    final var valueSetLabel = Optional.ofNullable(formItem.getValueSetId())
      .map(vsId -> {
        
        final var v = q.getValueSets().stream().filter(vs -> vs.getId().equals(vsId))
            .findFirst();

        if(v.isEmpty()) {
          return null;
        }
        
        final var entry = v.get().getEntries().stream()
            .filter(e -> e.getKey().equals(q.getMetadata().getLanguage()))
            .findFirst();
        
        return entry.get().getValue();
      });
    
    return ProxyAnswer.builder()
        .valueSetLabel(valueSetLabel)
        .formItem(formItem)
        .answer(answer)
        .build();
  }
  @Override
  public DialobSessionBuilderImpl createSession() {
    return new DialobSessionBuilderImpl(objectMapper, dialobService);
  }
  @Override
  public DialobProxy createProxy() {
    return new DialobProxyImpl(dialobService);
  }
  @Override
  public Questionnaire getQuestionnaireById(String questionnaireId) {
    return dialobService.getQuestionnaires().getForObject("/" + questionnaireId, Questionnaire.class);
  }
  @Override
  public Dialob getDialobById(String questionnaireId) {
    final var q = getQuestionnaireById(questionnaireId);
    return new QuestionnaireWrapperImpl(q);
  }
  @Override
  public Form createForm(Form form) {
    try {
      final var headers = headers().toSingleValueMap();
      final var body = objectMapper.writeValueAsString(form);
      final var resp = createProxy().formRequest("", null, HttpMethod.POST, body, headers);
      return objectMapper.readValue(resp.getBody(), Form.class);
    } catch (IOException e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  @Override
  public FormTag createTag(String formId, String tagName) {
    try {
      final var headers = headers().toSingleValueMap();
      final var body = objectMapper.writeValueAsString(ImmutableFormTag.builder()
          .name(tagName)
          .formName(formId)
          .type(Type.NORMAL)
          .build());
      final var postTagResp = createProxy().formRequest("/" + formId + "/tags", null, HttpMethod.POST, body, headers);
      DialobAssert.isTrue(postTagResp.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + postTagResp.getStatusCode() + " but expecting 2xx!");
      
      
      final var getTagResp = createProxy().formRequest("/" + formId + "/tags/" + tagName, null, HttpMethod.GET, body, headers);
      DialobAssert.isTrue(getTagResp.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getTagResp.getStatusCode() + " but expecting 2xx!");
      
      return objectMapper.readValue(getTagResp.getBody(), FormTag.class);
    } catch (IOException e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  @Override
  public Form getFormById(String formId) {
    return dialobService.getForms().getForObject("/" + formId, Form.class);
  }
  @Override
  public Form getFormByNameAndTag(String formName, String formTag) {
    return dialobService.getForms().getForObject("/" + formName + "/tags/" + formTag, Form.class);
  }
  @Override
  public Optional<Form> findOneFormById(String formId) {
    try {
      return Optional.ofNullable(dialobService.getForms().getForObject("/" + formId, Form.class));
    } catch(org.springframework.web.client.HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
  

  @Override
  public void completeSession(String sessionId) {

    DialobAssert.notNull(sessionId, () -> "sessionId can't be null!");
    try {
      final var headers = headers();
      
      final var getResponse = dialobService.getSessions().exchange(sessionId, HttpMethod.GET, new HttpEntity<String>(headers), JsonNode.class);
      DialobAssert.isTrue(getResponse.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getResponse.getStatusCode() + " but expecting 200!");
      
      
      // NEW -> OPEN
      String statusValue = getStatus(getResponse.getBody());
      if(statusValue.equals("NEW")) {
        
        ResponseEntity<String> response = dialobService.getSessions().exchange(sessionId, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(getResponse.getBody(), "OPEN"), headers), String.class);
        DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
      
        // OPEN -> COMPLETED
        ResponseEntity<JsonNode> openResponse = dialobService.getSessions().exchange(sessionId, HttpMethod.GET, new HttpEntity<String>(headers), JsonNode.class);
        ResponseEntity<String> completed = dialobService.getSessions().exchange(sessionId, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(openResponse.getBody(), "COMPLETED"), headers), String.class);
        DialobAssert.isTrue(completed.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
        
        return;
      } else if (statusValue.equals("COMPLETED")) {
        return;
        
      } else {
        ResponseEntity<String> response = dialobService.getSessions().exchange(sessionId, HttpMethod.PUT, new HttpEntity<JsonNode>(setStatus(getResponse.getBody(), "COMPLETED"), headers), String.class);
        DialobAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + response.getStatusCode() + " but expecting 200!");
        return;
      }
      
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }


  public Questionnaire.Metadata.Status getStatus(String sessionId) {
    DialobAssert.notNull(sessionId, () -> "sessionId can't be null!");
    try {

      ResponseEntity<String> getResponse = dialobService.getSessions().exchange(sessionId + "/status", HttpMethod.GET, new HttpEntity<String>(headers()), String.class);
      DialobAssert.isTrue(getResponse.getStatusCode().is2xxSuccessful(), () -> "DIALOB status was: " + getResponse.getStatusCode() + " but expecting 200!");
      
      String statusValue = getResponse.getBody().substring(1, getResponse.getBody().length() - 1);
      return statusValue.equals("COMPLETED") ? Questionnaire.Metadata.Status.COMPLETED : Questionnaire.Metadata.Status.OPEN;
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  
  private HttpHeaders headers() {
    final var headers = new HttpHeaders();
    headers.set(HttpHeaderNames.CONTENT_TYPE.toString(), MediaType.APPLICATION_JSON.toString());
    headers.set(HttpHeaderNames.ACCEPT.toString(), MediaType.APPLICATION_JSON.toString());
    return headers;
  } 
  
  private static String getStatus(JsonNode getBody) {
    ObjectNode metadata = (ObjectNode) getBody.get("metadata");
    String statusValue = metadata.get("status").textValue();
    return statusValue;
  }
  
  private static JsonNode setStatus(JsonNode getBody, String status) {
    ObjectNode metadata = (ObjectNode) getBody.get("metadata");
    metadata.set("status", TextNode.valueOf(status));
    return getBody;
  }

  public static Builder builder() {
    return new Builder();
  }
  
  @Setter
  @Accessors(fluent = true)
  public static class Builder {
    private ObjectMapper objectMapper;
    private DialobService dialobService;

    public DialobClientImpl build() {
      DialobAssert.notNull(objectMapper, () -> "objectMapper must be defined!");
      DialobAssert.notNull(dialobService, () -> "dialobService must be defined!");
      
      return new DialobClientImpl(objectMapper, dialobService);
    }
  }

  @Override
  public Questionnaire getQuestionnaireAndMetaById(String questionnaireId) {

    
    
    final var questionnaire = getQuestionnaireById(questionnaireId);
    final var form = getFormById(questionnaire.getMetadata().getFormId());
    final var valuesets = form.getValueSets().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    
    // re-map answers with form data
    final var answers = questionnaire.getAnswers().stream()
    .map(answer -> {
      final var meta = form.getData().get(answer.getId());
      final var valueset = meta.getValueSetId() == null ? null : valuesets.get(meta.getValueSetId());
  
      final Optional<FormValueSetEntry> valuesetEntry = valueset == null ? Optional.empty() : valueset.getEntries().stream().filter(entry -> entry.getId().equals(answer.getValue())).findFirst();    
      return new AnswerAndValueSet(answer, meta, Optional.ofNullable(valueset), valuesetEntry);
    })
    .collect(Collectors.toList());
    
    
    // group answers by value set id
    final var answerValueSets = answers.stream()
        .filter(a -> a.getValueSet().isPresent() && a.getValueSetEntry().isPresent())
        .collect(Collectors.groupingBy(a -> a.getValueSet().get().getId()));
    
    // apply corrections to value set 
    final var correctedValueSets = new ArrayList<ValueSet>();
    for(final var valueSet : questionnaire.getValueSets()) {
      final var mods = answerValueSets.get(valueSet.getId());
      final ValueSet merged = merge(valueSet, mods, questionnaire);
      correctedValueSets.add(merged);
    }
    
    // add answer meta data
    try {
      for(final var answer : answers) {
        correctedValueSets.add(ImmutableValueSet.builder()
            .id(LOOKUP + answer.getItem().getId())
            .addEntries(LOOKUP, objectMapper.writeValueAsString(answer.getItem()))
            .build());
      }
    } catch(Exception e) {
      // ignore failure on purpose
    }
    
    return ImmutableQuestionnaire.builder().from(questionnaire).valueSets(correctedValueSets).build();
  }
  
  public static FormItem LOOKUP(Answer answer, Questionnaire q, ObjectMapper om) {
    try {
      final var valueset = q.getValueSets().stream().filter(p -> p.getId().equals(LOOKUP + answer.getId())).findFirst().get();
      final var formItem = valueset.getEntries().iterator().next().getValue();
      return om.readValue(formItem, FormItem.class);
    } catch(Exception e) {
      // ignore failure on purpose
      
      return null;
    }
  }

  private ValueSet merge(ValueSet valueset, List<AnswerAndValueSet> valuesetCorrections, Questionnaire questionnaire) {
    if(valuesetCorrections == null) {
      return valueset;
    }
    
    final var correction = ImmutableValueSet.builder().from(valueset);
    final var existing = valueset.getEntries().stream().map(e -> e.getKey()).toList();
    for(final var corrections : valuesetCorrections) {
      final var key = corrections.getAnswer().getValue().toString();
      if(existing.contains(key)) {
        continue;
      }
      
      final var labels = corrections.getValueSetEntry().get().getLabel();
      
      for(final var label : labels.entrySet()) {
        correction.addEntries(ImmutableValueSetEntry.builder()
            .key(label.getKey())
            .value(label.getValue())
            .build());
      }
    }
    
    return correction.build();
  }
  
  
  @RequiredArgsConstructor
  @Data
  private static class AnswerAndValueSet {
    private final Answer answer;
    private final FormItem item;
    private final Optional<FormValueSet> valueSet;
    private final Optional<FormValueSetEntry> valueSetEntry; 
  }
}
