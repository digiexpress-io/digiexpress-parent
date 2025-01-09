package io.resys.thena.support;

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

import java.util.Map;
import java.util.function.Function;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbState;

public class OrgDbPrinter {
  private final DbState state;

  public OrgDbPrinter(DbState state) {
    super();
    this.state = state;
  }
  
  public String printWithStaticIds(Tenant repo, final Map<String, String> replacements) {

    final Function<String, String> ID = (id) -> {
    	if(id == null) {
    		return null;
    	}
    	
      if(replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };
  
    
    
    final var ctx = state.toOrgState(repo);
    StringBuilder result = new StringBuilder();
    
    result
    .append(System.lineSeparator())
    .append("Members").append(System.lineSeparator());
    
    ctx.query().members()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(ID.apply(item.getId())).append("    commitId: ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
        
        .append("    membername").append(": ").append(item.getUserName())
        .append(System.lineSeparator())

        .append("    email").append(": ").append(item.getEmail())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Parties").append(System.lineSeparator());
    ctx.query().parties()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(ID.apply(item.getId())).append("    commitId: ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())

        .append("    parentId").append(": ").append(ID.apply(item.getParentId()))
        .append(System.lineSeparator())
        
        .append("    partyName").append(": ").append(item.getPartyName())
        .append(System.lineSeparator())

        .append("    partyDescription").append(": ").append(item.getPartyDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    result
    .append(System.lineSeparator())
    .append("Memberships").append(System.lineSeparator());
    ctx.query().parties()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(ID.apply(item.getId())).append("    commitId: ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
        
        .append("    partyName").append(": ").append(item.getPartyName())
        .append(System.lineSeparator())

        .append("    partyDescription").append(": ").append(item.getPartyDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Rights").append(System.lineSeparator());
    ctx.query().rights()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(ID.apply(item.getId())).append("    commitId: ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
      

        .append("    rightName").append(": ").append(item.getRightName())
        .append(System.lineSeparator())
        
        .append("    rightDescription").append(": ").append(item.getRightDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    
    result
    .append(System.lineSeparator())
    .append("Member rights").append(System.lineSeparator());
    ctx.query().memberRights()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(ID.apply(item.getId())).append("    commitId: ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
        
        .append("    memberId").append(": ").append(ID.apply(item.getMemberId()))
        .append(System.lineSeparator())

        .append("    partyId").append(": ").append(ID.apply(item.getPartyId()))
        .append(System.lineSeparator())

        .append("    rightId").append(": ").append(ID.apply(item.getRightId()))
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Party rights").append(System.lineSeparator());
    ctx.query().partyRights()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(ID.apply(item.getId())).append("    commitId: ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
        
        .append("    partyId").append(": ").append(ID.apply(item.getPartyId()))
        .append(System.lineSeparator())

        .append("    rightId").append(": ").append(ID.apply(item.getRightId()))
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    return result.toString();
  }
  
  public String print(Tenant repo) {
   final var ctx = state.toOrgState(repo);
    
    StringBuilder result = new StringBuilder();
    result
    .append(System.lineSeparator())
    .append("Repo").append(System.lineSeparator())
    .append("  - id: ").append(repo.getId())
    .append(", rev: ").append(repo.getRev()).append(System.lineSeparator())
    .append("    name: ").append(repo.getName())
    .append(", prefix: ").append(repo.getPrefix())
    .append(", type: ").append(repo.getType()).append(System.lineSeparator());
    
    result
    .append(System.lineSeparator())
    .append("Members").append(System.lineSeparator());
    
    ctx.query().members()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getId()).append("    commitId: ").append(item.getCommitId())
        .append(System.lineSeparator())
        
        .append("    membername").append(": ").append(item.getUserName())
        .append(System.lineSeparator())

        .append("    email").append(": ").append(item.getEmail())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Parties").append(System.lineSeparator());
    ctx.query().parties()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getId()).append("    commitId: ").append(item.getCommitId())
        .append(System.lineSeparator())

        .append("    parentId").append(": ").append(item.getParentId())
        .append(System.lineSeparator())
        
        .append("    partyName").append(": ").append(item.getPartyName())
        .append(System.lineSeparator())

        .append("    partyDescription").append(": ").append(item.getPartyDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    result
    .append(System.lineSeparator())
    .append("Memberships").append(System.lineSeparator());
    ctx.query().parties()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getId()).append("    commitId: ").append(item.getCommitId())
        .append(System.lineSeparator())
        
        .append("    partyName").append(": ").append(item.getPartyName())
        .append(System.lineSeparator())

        .append("    partyDescription").append(": ").append(item.getPartyDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Rights").append(System.lineSeparator());
    ctx.query().rights()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getId()).append("    commitId: ").append(item.getCommitId())
        .append(System.lineSeparator())
      

        .append("    rightName").append(": ").append(item.getRightName())
        .append(System.lineSeparator())
        
        .append("    rightDescription").append(": ").append(item.getRightDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    
    result
    .append(System.lineSeparator())
    .append("Member rights").append(System.lineSeparator());
    ctx.query().memberRights()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getId()).append("    commitId: ").append(item.getCommitId())
        .append(System.lineSeparator())
        
        .append("    memberId").append(": ").append(item.getMemberId())
        .append(System.lineSeparator())
        
        .append("    partyId").append(": ").append(item.getPartyId())
        .append(System.lineSeparator())

        .append("    rightId").append(": ").append(item.getRightId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Party rights").append(System.lineSeparator());
    ctx.query().partyRights()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getId()).append("    commitId: ").append(item.getCommitId())
        .append(System.lineSeparator())
        
        .append("    partyId").append(": ").append(item.getPartyId())
        .append(System.lineSeparator())

        .append("    rightId").append(": ").append(item.getRightId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    return result.toString();
  }
  
  
}
