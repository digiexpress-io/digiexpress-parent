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

import org.immutables.value.Value;

import io.resys.hdes.executor.spi.commnd.HdesCommand;
import io.resys.hdes.runtime.test.ParseSessionCommand.ParseSessionCommandMapping;
import io.resys.hdes.runtime.test.ParseSessionCommand.ParseSessionCommandOutput;

public class ParseSessionCommand implements HdesCommand<ParseSessionCommandMapping, ParseSessionCommandOutput> {

  @Value.Immutable
  public interface ParseSessionCommandMapping extends HdesCommand.Mapping {
    String getFormId();
  }

  @Value.Immutable
  public interface ParseSessionCommandOutput extends HdesCommand.Output {
    String getUserDecision();
  } 
  
  @Override
  public ParseSessionCommandOutput accept(Serializable accepts, ParseSessionCommandMapping mapping) {
    return null;
  }

}
