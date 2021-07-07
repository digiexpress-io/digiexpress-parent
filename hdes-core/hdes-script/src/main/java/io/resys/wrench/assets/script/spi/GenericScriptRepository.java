package io.resys.wrench.assets.script.spi;

/*-
 * #%L
 * wrench-assets-script
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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

import freemarker.template.Configuration;
import io.resys.wrench.assets.datatype.api.DataTypeRepository;
import io.resys.wrench.assets.script.api.ScriptRepository;
import io.resys.wrench.assets.script.spi.builders.GenericScriptModelBuilder;
import io.resys.wrench.assets.script.spi.builders.GroovyScriptBuilder;
import io.resys.wrench.assets.script.spi.builders.GroovyScriptParser;

public class GenericScriptRepository implements ScriptRepository {
  private final DataTypeRepository dataTypeRepository;
  private final Configuration cfg;
  private final GroovyScriptParser scriptParsers;
  private final ScriptConstructor scriptConstructor;

  public GenericScriptRepository(
      ScriptConstructor scriptConstructor,
      GroovyScriptParser scriptParsers, 
      Configuration cfg, 
      DataTypeRepository dataTypeRepository) {
    super();
    this.scriptConstructor = scriptConstructor;
    this.dataTypeRepository = dataTypeRepository;
    this.cfg = cfg;
    this.scriptParsers = scriptParsers;
  }

  @Override
  public ScriptBuilder createBuilder() {
    return new GroovyScriptBuilder(scriptConstructor, scriptParsers, dataTypeRepository, cfg, () -> new GenericScriptModelBuilder());
  }
}
