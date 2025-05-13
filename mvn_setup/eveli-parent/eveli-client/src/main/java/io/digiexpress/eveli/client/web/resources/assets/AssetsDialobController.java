package io.digiexpress.eveli.client.web.resources.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.dialob.api.DialobClient.FormListItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/worker/rest/api/assets/dialob")
@RequiredArgsConstructor
public class AssetsDialobController {
  
  private final DialobClient dialobCommands;


  @RequestMapping(path="/proxy/api/forms/**", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> proxy(
      HttpServletRequest request, 
      @RequestBody(required = false) String body,
      @RequestHeader Map<String, String> headers
  ) {
    
    final var query = request.getQueryString();
    final var path = request.getServletPath().substring(46);
    final var method = HttpMethod.valueOf(request.getMethod());
    
    return dialobCommands.createProxyClient().formRequest(path, query, method, body, headers);
  }
 
  @GetMapping(path="/tags", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<FormTagResult>> allTags() throws JsonMappingException, JsonProcessingException {
    List<FormTagResult> tags = new ArrayList<>();
    
    final var forms = dialobCommands.findAllForms();
    Map<String, @NotNull String> formLabels = forms.stream().collect(Collectors.toMap(val->val.getId(), val->val.getMetadata().getLabel()));
    final var formTags = dialobCommands.findAllFormTags();
    for (var formTag : formTags) {
      FormTagResult result = new FormTagResult();
      result.setFormName(formTag.getFormName());
      result.setFormLabel(formLabels.get(formTag.getFormName()));
      result.setTagFormId(formTag.getFormId());
      result.setTagName(formTag.getName());
      tags.add(result);
    }
    return ResponseEntity.status(HttpStatus.OK).body(tags);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FormListItem> allForms() throws JsonMappingException, JsonProcessingException{
    return dialobCommands.findAllForms();
  }



  
  @Data
  public static class FormTagResult {
    private String formLabel;
    private String formName;
    private String tagFormId;
    private String tagName;
  }
}
