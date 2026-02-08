package io.resys.thena.fs.spi.snapshot;

import java.util.List;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.LogConstants;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tree;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_COMMIT)
public class SnapshotLogger {
  private final StringBuilder data = new StringBuilder();

  
  public SnapshotLogger rmNodes(List<Node> nodes) {
    
  }
  
  public SnapshotLogger newTree(Tree next) {
    
  }

  public SnapshotLogger mergeTree(Tree prev, Tree next) {
    
  }
  
  public SnapshotLogger newCommit(Commit next) {
    
  }
  
  public SnapshotLogger mergeCommit(Commit prev, Commit next) {
    
  }
  
  public SnapshotLogger newBranch(Ref ref) {
    
  }
  public SnapshotLogger mergeBranch(Ref prev, Ref next) {
    
  }
  
  public SnapshotLogger append(String data) {
    if(log.isDebugEnabled()) {
      this.data.append(data);
    }
    return this;
  }
  @Override
  public String toString() {
    if(log.isDebugEnabled()) {
      log.debug(data.toString());
    } else {
      data.append("Log DEBUG disabled for: " + LogConstants.SHOW_COMMIT + "!");
    }
    return data.toString();
  }

  

} 
