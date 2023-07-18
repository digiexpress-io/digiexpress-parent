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

import java.io.Serializable;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.hdes.executor.spi.commnd.HdesCommand;
import io.resys.hdes.executor.spi.commnd.HdesCommandPromise;
import io.resys.hdes.runtime.test.CreateSessionCommand.CreateSessionCommandMapping;
import io.resys.hdes.runtime.test.CreateSessionCommand.CreateSessionCommandOutput;

public class CreateSessionCommand implements HdesCommandPromise<CreateSessionCommandMapping, CreateSessionCommandOutput> {

  @Value.Immutable
  public interface CreateSessionCommandMapping extends HdesCommand.Mapping {
    String getFormId();
  }

  @Value.Immutable
  public interface CreateSessionCommandOutput extends HdesCommand.Output {
    String getDataId();
    Integer getUserValue();
  }
  
  @Override
  public String onEnter(Serializable accepts, CreateSessionCommandMapping mapping) {
    return SessionDB.get().create();
  }

  @Override
  public CreateSessionCommandOutput onComplete(
      String dataId, Serializable data, Serializable accepts, CreateSessionCommandMapping mapping) {
    
    Map<String, Serializable> result = (Map<String, Serializable>) data;
    
    return ImmutableCreateSessionCommandOutput.builder()
        .dataId(dataId)
        .userValue((Integer) result.get("userValue"))
        .build();
  }

  @Override
  public CreateSessionCommandOutput onError(String dataId, Serializable accepts, CreateSessionCommandMapping mapping) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CreateSessionCommandOutput onTimeout(String dataId, long timeout, Serializable accepts,
      CreateSessionCommandMapping mapping) {
    // TODO Auto-generated method stub
    return null;
  }
}
