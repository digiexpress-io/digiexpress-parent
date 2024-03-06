package io.resys.thena.docdb.support;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.DbState;

public class OrgDbPrinter {
  private final DbState state;

  public OrgDbPrinter(DbState state) {
    super();
    this.state = state;
  }
  
  public String printWithStaticIds(Repo repo) {
    final Map<String, String> replacements = new HashMap<>();
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
  
    final var ctx = state.toOrgState().withRepo(repo);
    StringBuilder result = new StringBuilder();
    
    result
    .append(System.lineSeparator())
    .append("Users").append(System.lineSeparator());
    
    ctx.query().users()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
        .append(ID.apply(item.getId()))
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
        
        .append("    username").append(": ").append(item.getUserName())
        .append(System.lineSeparator())

        .append("    email").append(": ").append(item.getEmail())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Groups").append(System.lineSeparator());
    ctx.query().groups()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
      	.append(ID.apply(item.getId()))
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())

        .append("    parentId").append(": ").append(ID.apply(item.getParentId()))
        .append(System.lineSeparator())
        
        .append("    groupName").append(": ").append(item.getGroupName())
        .append(System.lineSeparator())

        .append("    groupDescription").append(": ").append(item.getGroupDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    result
    .append(System.lineSeparator())
    .append("Group Members").append(System.lineSeparator());
    ctx.query().groups()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
      	.append(ID.apply(item.getId()))
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
        
        .append("    groupName").append(": ").append(item.getGroupName())
        .append(System.lineSeparator())

        .append("    groupDescription").append(": ").append(item.getGroupDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Roles").append(System.lineSeparator());
    ctx.query().roles()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
      	.append(ID.apply(item.getId()))
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
      

        .append("    roleName").append(": ").append(item.getRoleName())
        .append(System.lineSeparator())
        
        .append("    roleDescription").append(": ").append(item.getRoleDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    
    result
    .append(System.lineSeparator())
    .append("User roles").append(System.lineSeparator());
    ctx.query().userRoles()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
      	.append(ID.apply(item.getId()))
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
        
        .append("    userId").append(": ").append(ID.apply(item.getUserId()))
        .append(System.lineSeparator())

        .append("    roleId").append(": ").append(ID.apply(item.getRoleId()))
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Group roles").append(System.lineSeparator());
    ctx.query().groupRoles()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
      	.append(ID.apply(item.getId()))
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
        
        .append("    groupId").append(": ").append(ID.apply(item.getGroupId()))
        .append(System.lineSeparator())

        .append("    roleId").append(": ").append(ID.apply(item.getRoleId()))
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Actors status").append(System.lineSeparator());
    ctx.query().actorStatus()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
        .append(ID.apply(item.getId()))
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(ID.apply(item.getCommitId()))
        .append(System.lineSeparator())
        
        .append("    groupId").append(": ").append(ID.apply(item.getGroupId()))
        .append(System.lineSeparator())

        .append("    roleId").append(": ").append(ID.apply(item.getRoleId()))
        .append(System.lineSeparator())
         
        .append("    status").append(": ").append(item.getValue())
        .append(System.lineSeparator())
        ;
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    return result.toString();
  }
  
  public String print(Repo repo) {
   final var ctx = state.toOrgState().withRepo(repo);
    
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
    .append("Users").append(System.lineSeparator());
    
    ctx.query().users()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
        .append(item.getId())
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(item.getCommitId())
        .append(System.lineSeparator())
        
        .append("    username").append(": ").append(item.getUserName())
        .append(System.lineSeparator())

        .append("    email").append(": ").append(item.getEmail())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Groups").append(System.lineSeparator());
    ctx.query().groups()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
        .append(item.getId())
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(item.getCommitId())
        .append(System.lineSeparator())

        .append("    parentId").append(": ").append(item.getParentId())
        .append(System.lineSeparator())
        
        .append("    groupName").append(": ").append(item.getGroupName())
        .append(System.lineSeparator())

        .append("    groupDescription").append(": ").append(item.getGroupDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    result
    .append(System.lineSeparator())
    .append("Group Members").append(System.lineSeparator());
    ctx.query().groups()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
        .append(item.getId())
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(item.getCommitId())
        .append(System.lineSeparator())
        
        .append("    groupName").append(": ").append(item.getGroupName())
        .append(System.lineSeparator())

        .append("    groupDescription").append(": ").append(item.getGroupDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Roles").append(System.lineSeparator());
    ctx.query().roles()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
        .append(item.getId())
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(item.getCommitId())
        .append(System.lineSeparator())
      

        .append("    roleName").append(": ").append(item.getRoleName())
        .append(System.lineSeparator())
        
        .append("    roleDescription").append(": ").append(item.getRoleDescription())
        .append(System.lineSeparator())

        .append("    externalId").append(": ").append(item.getExternalId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    
    result
    .append(System.lineSeparator())
    .append("User roles").append(System.lineSeparator());
    ctx.query().userRoles()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
        .append(item.getId())
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(item.getCommitId())
        .append(System.lineSeparator())
        
        .append("    userId").append(": ").append(item.getUserId())
        .append(System.lineSeparator())

        .append("    roleId").append(": ").append(item.getRoleId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Group roles").append(System.lineSeparator());
    ctx.query().groupRoles()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
        .append(item.getId())
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(item.getCommitId())
        .append(System.lineSeparator())
        
        .append("    groupId").append(": ").append(item.getGroupId())
        .append(System.lineSeparator())

        .append("    roleId").append(": ").append(item.getRoleId())
        .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    result
    .append(System.lineSeparator())
    .append("Actors status").append(System.lineSeparator());
    ctx.query().actorStatus()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
        .append(item.getId())
        .append(System.lineSeparator())

        .append("    commitId").append(": ").append(item.getCommitId())
        .append(System.lineSeparator())
        
        .append("    groupId").append(": ").append(item.getGroupId())
        .append(System.lineSeparator())

        .append("    roleId").append(": ").append(item.getRoleId())
        .append(System.lineSeparator())
         
        .append("    status").append(": ").append(item.getValue())
        .append(System.lineSeparator())
        ;
      
      return item;
    }).collect().asList().await().indefinitely();
    
    return result.toString();
  }
  
  
}
