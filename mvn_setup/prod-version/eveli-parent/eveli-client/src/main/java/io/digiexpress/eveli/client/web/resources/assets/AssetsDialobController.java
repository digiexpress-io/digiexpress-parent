package io.digiexpress.eveli.client.web.resources.assets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/*-
 * #%L
 * eveli-client
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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.form.FormTag;
import io.digiexpress.eveli.client.api.DialobCommands;
import io.digiexpress.eveli.client.api.DialobCommands.FormListItem;
import io.digiexpress.eveli.client.api.DialobCommands.FormTagResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/dialob-assets")
@RequiredArgsConstructor
public class AssetsDialobController {
  
  private final DialobCommands dialobCommands;
  private final ObjectMapper objectMapper;


  @RequestMapping(path="/dialob-proxy/**", produces = "application/json; charset=UTF-8")
  public ResponseEntity<?> proxy(
      HttpServletRequest request, 
      @RequestBody(required = false) String body,
      @RequestHeader Map<String, String> headers
  ) {
    
    final var query = request.getQueryString();
    final var path = request.getServletPath().substring(28);
    final var method = HttpMethod.valueOf(request.getMethod());
    
    return dialobCommands.createProxy().anyRequest(path, query, method, body, headers);
  }
 
  @GetMapping(path="/tags", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<FormTagResult>> allTags() throws JsonMappingException, JsonProcessingException {
    List<FormTagResult> tags = new ArrayList<>();
    
    FormListItem[] forms = getForms();
    for (var form : forms) {
      FormTag[] formTags = getTags(form.getId());
      for (var formTag : formTags) {
        FormTagResult result = new FormTagResult();
        result.setFormName(form.getId());
        result.setFormLabel(form.getMetadata().getLabel());
        result.setTagFormId(formTag.getFormId());
        result.setTagName(formTag.getName());
        tags.add(result);
      }
    };
    return ResponseEntity.status(HttpStatus.OK).body(tags);
  }

  @GetMapping(path="/", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FormListItem> allForms() throws JsonMappingException, JsonProcessingException{
    FormListItem[] forms = getForms();

    return Arrays.asList(forms);
  }
  
  
  


  private FormTag[] getTags(String id) throws JsonMappingException, JsonProcessingException {
    final var uri = "api/forms/" + id + "/tags";
    final String body = dialobCommands.createProxy().anyRequest(uri, "", HttpMethod.GET, null, Collections.emptyMap()).getBody();
    return objectMapper.readerForArrayOf(FormTag.class).readValue(body);
  }

  private FormListItem[] getForms() throws JsonMappingException, JsonProcessingException {
    final var uri = "api/forms";
    final String body = dialobCommands.createProxy().anyRequest(uri, "", HttpMethod.GET, null, Collections.emptyMap()).getBody();
    return objectMapper.readerForArrayOf(FormListItem.class).readValue(body);
  }
}
