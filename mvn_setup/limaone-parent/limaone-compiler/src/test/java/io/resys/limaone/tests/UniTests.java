package io.resys.limaone.tests;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

public class UniTests {

  
  @Test
  public void test() {

    final var xx = Uni.createFrom().item(() -> {
      return find();
    }).await().atMost(Duration.ofMillis(1000));
    
    System.out.println(xx);
  }
  
  
  public String find() {
    return Uni.createFrom().item(() -> "Hello")
    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
    .await().atMost(Duration.ofMillis(1000));
  }
}
