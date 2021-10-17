package io.resys.hdes.client.test;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.Program.ProgramSupplier;
import io.resys.hdes.client.api.programs.ServiceProgram;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesTypeDefsFactory.ServiceInit;



public class TestUtils {

  public static ObjectMapper objectMapper = new ObjectMapper();
  public static HdesClient client = HdesClientImpl.builder()
      .objectMapper(objectMapper)
      .programSupplier(new ProgramSupplier() {
        @Override
        public ServiceProgram getService(String name) {
          // TODO Auto-generated method stub
          return null;
        }
        @Override
        public FlowProgram getFlow(String name) {
          // TODO Auto-generated method stub
          return null;
        }
        @Override
        public DecisionProgram getDecision(String name) {
          // TODO Auto-generated method stub
          return null;
        }
      })
      .serviceInit(new ServiceInit() {
        @Override
        public <T> T get(Class<T> type) {
          try {
            return type.getDeclaredConstructor().newInstance();
          } catch(Exception e) {
            throw new RuntimeException(e.getMessage(), e);
          }
        }
      })
      .build();
}
