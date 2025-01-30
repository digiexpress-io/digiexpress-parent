package io.digiexpress.thena.mq.client.api.routing;

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

import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Router {
  private static String SPLIT_AT_DOT = "\\.";
  private static String SPLIT_AT_SLASH = "\\/";
  private final String[] queueName;
  private final String[] routingKey;
  private int queueIndex = 0;
  private int routingIndex = 0;

  public boolean accept() {
    RepoAssert.isTrue(queueName.length > 0, () -> "queueName must contain at least on non empty name");
    RepoAssert.isTrue(routingKey.length > 0, () -> "routingKey must contain at least on non empty name");
    
    while(this.queueIndex < this.queueName.length) {
      final var queueName = visitQueue();
      final var routingKey = visitRoutingKey();
         
      final var isMatch = visitMatching(queueName, routingKey);
      final var moreInQueue = isMoreInQueue();
      final var moreInRouting = isMoreInRouting();   
      if(!isMatch) {
        return false;
      }
      
      
      // both have reached the end
      if(!moreInQueue && !moreInRouting) {
        return true;
      }
      
      
      // there are more in the queue but routing is at the end and matches wildcard
      if(moreInQueue && !moreInRouting && routingKey.equals("#")) {
        return true;
      }
      
      if(moreInQueue && !moreInRouting) {
        return false;
      }
    }
    
    return false;
  }
  
  private boolean isMoreInQueue() {
    return this.queueIndex < this.queueName.length;
  }

  private boolean isMoreInRouting() {
    return this.routingIndex < this.routingKey.length;
  }
  
  // * (star) can substitute for exactly one word.
  // # (hash) can substitute for zero or more words.
  private boolean visitMatching(String queueName, String routingKey) {
    if(routingKey.equals("#")) {
      return visitHashMatching(queueName, routingKey);
    }
    if(routingKey.equals("*")) {
      return true;
    }
    return queueName.equalsIgnoreCase(routingKey);
  }

  private boolean visitHashMatching(String queueName, String routingKey) {
    // routing key is star
    RepoAssert.isTrue(routingKey.equals("#"), () -> "routing key element can be only be # in here!");
    final var nextRoutingKey = visitRoutingKey();
    
    // does'nt matter anymore, match the ending of the queue with everything
    if(nextRoutingKey == null) {
      return true; //BUG
    }
    
    String nextQueueName;
    boolean isNextMatch = false;
    while((nextQueueName = visitQueue()) != null) {
      if(isNextMatch = visitMatching(nextQueueName, nextRoutingKey)) {
        break;
      } 
    }
    return isNextMatch;
  }

  private String visitQueue() {
    if(queueName.length <= queueIndex) {
      return null;
    }
    final var currentQueueName = queueName[queueIndex++];
    return currentQueueName;
  }
  
  private String visitRoutingKey() {
    if(routingKey.length <= routingIndex) {
      return null;
    }
    final var currentRoutingKey = routingKey[routingIndex++];
    return currentRoutingKey;
  }
  
  public static RouterBuilder builderWithDot() {
    return new RouterBuilder(SPLIT_AT_DOT);
  }
  public static RouterBuilder builderWithSlash() {
    return new RouterBuilder(SPLIT_AT_SLASH);
  }
  
  @RequiredArgsConstructor
  public static class RouterBuilder {
    private String[] queueName;
    private String[] routingKey;
    private final String splitAt;
    
    
    private String[] split(String value) {
      return value.split(splitAt);
    }
    public RouterBuilder queueName(String queueName) {
      this.queueName = split(queueName);
      return this;
    }
    public RouterBuilder queueName(String[] queueName) {
      this.queueName = queueName;
      return this;
    }
    public RouterBuilder routingKey(String routingKey) {
      this.routingKey = split(routingKey);
      return this;
    }
    public RouterBuilder routingKey(String[] routingKey) {
      this.routingKey = routingKey;
      return this;
    }
    public Router build() {
      return new Router(queueName, routingKey);
    }
    public boolean isMatch() {
      return new Router(queueName, routingKey).accept();
    }
  }
}
