package io.resys.hdes.client.spi.branch;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.client.api.ast.AstBranch;
import io.resys.hdes.client.api.programs.BranchProgram;
import io.resys.hdes.client.api.programs.ImmutableBranchProgram;
import io.resys.hdes.client.spi.config.HdesClientConfig;

public class BranchProgramBuilder {
  private final HdesClientConfig config;

  public BranchProgramBuilder(HdesClientConfig config) {
    super();
    this.config = config;
  }
  public BranchProgram build(AstBranch ast) {
    return ImmutableBranchProgram.builder().build();
  }
}
