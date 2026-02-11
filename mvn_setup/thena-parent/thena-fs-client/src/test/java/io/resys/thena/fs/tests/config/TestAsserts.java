package io.resys.thena.fs.tests.config;

/*-
 * #%L
 * thena-fs-client
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

import org.junit.jupiter.api.Assertions;

import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.fs.api.commits.CommitBuilder.CommitResult;

public class TestAsserts {

  
  public static void assertEqualsCodeAndMessage(
      CommitResult result,
      
      CommitResultStatus expectedStatus,
      String expectedCode) {
    
    Assertions.assertEquals(expectedStatus, result.getStatus());
    
    final var count = result.getMessages().stream()
      .filter(msg -> msg.getText().contains(expectedCode))
      .count();
    
    Assertions.assertEquals(expectedStatus, result.getStatus());
    Assertions.assertEquals(count, 1);
  }
  
  
  public static void assertLog(CommitResult result, String expected) {
   
    final var norm = new TestLogDataNormalizer();
    
    Assertions.assertEquals(expected, norm.normalizeLogData(result.getLog()));
  }
}
