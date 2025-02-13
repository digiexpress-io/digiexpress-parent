package io.digiexpress.eveli.client.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.Setter;


@Data @Setter
@ConfigurationProperties(prefix = "eveli.feedback")
public class EveliPropsFeedback {  
  private Boolean enabled;
  
  // all of the value can have multiple values separated by ","
  
  private String forms; // forms for what feedback is enabled
  private String categoryMain; // answer from what to extract main category value
  private String categorySub; // answer from what to extract sub category value
  private String questionTitle; // answer from what to extract title
  private String question;// answer from what to extract question 
  private String username;// answer/context var from what to extract customer name 
  private String usernameAllowed;// answer from what to extract if username is allowed
  
  
  public static List<String> toList(String input) {
    if(input == null || input.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.asList(input.split(",")).stream().map(e -> e.trim()).toList();
  }
  public Boolean getEnabled() {
    return enabled;
  }
  public List<String> getForms() {
    return toList(forms);
  }
  public List<String> getCategoryMain() {
    return toList(categoryMain);
  }
  public List<String> getCategorySub() {
    return toList(categorySub);
  }
  public List<String> getQuestionTitle() {
    return toList(questionTitle);
  }
  public List<String> getQuestion() {
    return toList(question);
  }
  public List<String> getUsername() {
    return toList(username);
  }
  public List<String> getUsernameAllowed() {
    return toList(usernameAllowed);
  }
}
