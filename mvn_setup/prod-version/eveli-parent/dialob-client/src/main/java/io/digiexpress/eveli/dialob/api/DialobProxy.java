package io.digiexpress.eveli.dialob.api;

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

import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;


// whatever api 
public interface DialobProxy {
  
  ResponseEntity<String> sessionGet(String sessionId);
  ResponseEntity<String> sessionPost(String sessionId, String body);
  
  
  // path = anything that comes after forms/api
  ResponseEntity<String> formRequest(String path, String query, HttpMethod method, String body, Map<String, String> headers);


}
