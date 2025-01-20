package io.digiexpress.thena.mq.test;

/*-
 * #%L
 * thena-mq-client
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.digiexpress.thena.mq.client.api.routing.Router;

public class RoutingKeyMatchTest {

  
  @Test
  public void testSingleWordMatchWithDot() {
    Assertions.assertTrue(Router.builderWithDot().queueName("task.modify.name").routingKey("*.modify.name").isMatch());
    Assertions.assertTrue(Router.builderWithDot().queueName("task.modify.name").routingKey("task.*.name").isMatch());
    Assertions.assertTrue(Router.builderWithDot().queueName("task.modify.name").routingKey("task.modify.*").isMatch());
    Assertions.assertTrue(Router.builderWithDot().queueName("task.modify.name").routingKey("*.*.*").isMatch());
    
    Assertions.assertFalse(Router.builderWithDot().queueName("task.modify.description").routingKey("task").isMatch());    
    Assertions.assertFalse(Router.builderWithDot().queueName("task.modify.name").routingKey("*").isMatch());
    Assertions.assertFalse(Router.builderWithDot().queueName("task.modify.name").routingKey("*.*").isMatch());
    Assertions.assertFalse(Router.builderWithDot().queueName("task.modify.description").routingKey("task.*.name").isMatch());    
    Assertions.assertFalse(Router.builderWithDot().queueName("taskcomment.modify.description").routingKey("task.*.name").isMatch());
  }
  
  
  @Test
  public void testWildcardWordMatchWithDot() {
    Assertions.assertFalse(Router.builderWithDot().queueName("taskcomment.modify.description").routingKey("#.name").isMatch());
    Assertions.assertTrue(Router.builderWithDot().queueName("task.modify.description").routingKey("#.description").isMatch());
    
    
    Assertions.assertTrue(Router.builderWithDot().queueName("task.modify.description").routingKey("#.modify.description").isMatch());
    Assertions.assertTrue(Router.builderWithDot().queueName("task.modify.description").routingKey("task.#").isMatch());
    Assertions.assertTrue(Router.builderWithDot().queueName("task.modify.description").routingKey("task.#.description").isMatch());
    Assertions.assertTrue(Router.builderWithDot().queueName("task.modify.description").routingKey("task.modify.#").isMatch());
    Assertions.assertTrue(Router.builderWithDot().queueName("task.modify.description").routingKey("#").isMatch());
    
  }
  
  
  @Test
  public void testSingleWordMatchWithSlash() {
    Assertions.assertTrue(Router.builderWithSlash().queueName("task/modify/name").routingKey("*/modify/name").isMatch());
    Assertions.assertTrue(Router.builderWithSlash().queueName("task/modify/name").routingKey("task/*/name").isMatch());
    Assertions.assertTrue(Router.builderWithSlash().queueName("task/modify/name").routingKey("task/modify/*").isMatch());
    Assertions.assertTrue(Router.builderWithSlash().queueName("task/modify/name").routingKey("*/*/*").isMatch());
    
    Assertions.assertFalse(Router.builderWithSlash().queueName("task/modify/description").routingKey("task").isMatch());    
    Assertions.assertFalse(Router.builderWithSlash().queueName("task/modify/name").routingKey("*").isMatch());
    Assertions.assertFalse(Router.builderWithSlash().queueName("task/modify/name").routingKey("*/*").isMatch());
    Assertions.assertFalse(Router.builderWithSlash().queueName("task/modify/description").routingKey("task/*/name").isMatch());    
    Assertions.assertFalse(Router.builderWithSlash().queueName("taskcomment/modify/description").routingKey("task/*/name").isMatch());
  }
  
  
  @Test
  public void testWildcardWordMatchWithSlash() {
    Assertions.assertTrue(Router.builderWithSlash().queueName("/comments/0/external/true").routingKey("/comments/*/external/true").isMatch());
    Assertions.assertTrue(Router.builderWithSlash().queueName("/assignedUser/sam").routingKey("/assignedUser/*").isMatch());
    
    Assertions.assertFalse(Router.builderWithSlash().queueName("taskcomment/modify/description").routingKey("#/name").isMatch());
    Assertions.assertTrue(Router.builderWithSlash().queueName("task/modify/description").routingKey("#/description").isMatch());
    
    
    Assertions.assertTrue(Router.builderWithSlash().queueName("task/modify/description").routingKey("#/modify/description").isMatch());
    Assertions.assertTrue(Router.builderWithSlash().queueName("task/modify/description").routingKey("task/#").isMatch());
    Assertions.assertTrue(Router.builderWithSlash().queueName("task/modify/description").routingKey("task/#/description").isMatch());
    Assertions.assertTrue(Router.builderWithSlash().queueName("task/modify/description").routingKey("task/modify/#").isMatch());
    Assertions.assertTrue(Router.builderWithSlash().queueName("task/modify/description").routingKey("#").isMatch());    
    
  }
}
