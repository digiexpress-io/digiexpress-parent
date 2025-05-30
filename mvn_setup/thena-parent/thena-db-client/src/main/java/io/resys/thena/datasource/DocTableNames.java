package io.resys.thena.datasource;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import org.immutables.value.Value;

import io.resys.thena.api.entities.Tenant;

@Value.Immutable
public abstract class DocTableNames {
  private static final DocTableNames DEFAULTS = defaults();
  
  public abstract String getPrefix();

  
  // doc structures
  public abstract String getDocCommands();
  public abstract String getDocCommits();
  public abstract String getDocBranch();
  public abstract String getDocLog();
  public abstract String getDoc();
  

  
  public DocTableNames toRepo(Tenant repo) {
    final String prefix = repo.getPrefix();
    return toRepo(prefix);
  }
  
  public DocTableNames toRepo(String prefix) {
    return ImmutableDocTableNames.builder()
        .prefix(prefix)
        
        .docCommands(prefix + DEFAULTS.getDocCommands())
        .docCommits(prefix + DEFAULTS.getDocCommits())
        .docBranch( prefix + DEFAULTS.getDocBranch())
        .docLog(    prefix + DEFAULTS.getDocLog())
        .doc(       prefix + DEFAULTS.getDoc())
                
        .build();
  }
  
  public static DocTableNames defaults() {
    return ImmutableDocTableNames.builder()
      .prefix("")
      
      .docCommands("doc_commands")
      .docCommits("doc_commits")
      .docBranch("doc_branch")
      .docLog("doc_log")
      .doc("doc")

      .build();
  }
}
