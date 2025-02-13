package io.resys.hdes.runtime.test;

/*-
 * #%L
 * hdes-runtime-test
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.immutables.value.Value;

public class SessionDB {
  private static final SessionDB instance = new SessionDB();
  
  private List<Session> sessions = new ArrayList<>();
  
  @Value.Immutable
  public interface Session {
    String getDataId();
  }
  
  public static SessionDB get() {
    return instance;
  }
  
  public String create() {
    Session value = ImmutableSession.builder().dataId(UUID.randomUUID().toString()).build();
    sessions.add(value);
    return value.getDataId();
  }
}
