package io.resys.thena.storesql;

import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlSchema;
import io.resys.thena.storesql.support.SqlStatement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlSchemaImpl implements SqlSchema {

  protected final TenantTableNames options;
  
  @Override
  public Sql createTenant() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getTenant()).ln()
        .append("(").ln()
        .append("  id VARCHAR(40) PRIMARY KEY,").ln()
        .append("  rev VARCHAR(40) NOT NULL,").ln()
        .append("  prefix VARCHAR(40) NOT NULL,").ln()
        .append("  type VARCHAR(3) NOT NULL,").ln()
        .append("  name VARCHAR(255) NOT NULL,").ln()
        .append("  external_id VARCHAR(255),").ln()
        .append("  UNIQUE(name), UNIQUE(rev), UNIQUE(prefix), UNIQUE(external_id)").ln()
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getTenant()).append("_NAME_INDEX")
        .append(" ON ").append(options.getTenant()).append(" (name);").ln()
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getTenant()).append("_EXT_INDEX")
        .append(" ON ").append(options.getTenant()).append(" (external_id);").ln()
        
        .build()).build();
  }
  
  @Override
  public SqlSchemaImpl withTenant(TenantTableNames options) {
    return new SqlSchemaImpl(options);
  }

  @Override
  public Sql createDoc() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDoc()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  external_id_deleted VARCHAR(40),").ln()
    .append("  owner_id VARCHAR(40),").ln()
    .append("  doc_parent_id VARCHAR(40),").ln()
    .append("  doc_type VARCHAR(40) NOT NULL,").ln()
    .append("  doc_status VARCHAR(8) NOT NULL,").ln()
    .append("  doc_meta jsonb").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getDoc()).append("_DOC_EXT_ID_INDEX")
    .append(" ON ").append(options.getDoc()).append(" (external_id);").ln()

    .append("CREATE INDEX ").append(options.getDoc()).append("_DOC_PARENT_ID_INDEX")
    .append(" ON ").append(options.getDoc()).append(" (doc_parent_id);").ln()

    .append("CREATE INDEX ").append(options.getDoc()).append("_DOC_TYPE_INDEX")
    .append(" ON ").append(options.getDoc()).append(" (doc_type);").ln()

    .append("CREATE INDEX ").append(options.getDoc()).append("_DOC_OWNER_INDEX")
    .append(" ON ").append(options.getDoc()).append(" (owner_id);").ln()

    // internal foreign key
    .append("ALTER TABLE ").append(options.getDoc()).ln()
    .append("  ADD CONSTRAINT ").append(options.getDoc()).append("_DOC_PARENT_FK").ln()
    .append("  FOREIGN KEY (doc_parent_id)").ln()
    .append("  REFERENCES ").append(options.getDoc()).append(" (id);").ln().ln()
      
    .build()).build();
  }

  @Override
  public Sql createDocBranch() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDocBranch()).ln()
    .append("(").ln()
    .append("  branch_name VARCHAR(255) NOT NULL,").ln()
    .append("  branch_name_deleted VARCHAR(255),").ln()
    .append("  branch_id VARCHAR(40) NOT NULL,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  branch_status VARCHAR(8) NOT NULL,").ln()
    .append("  doc_id VARCHAR(40),").ln()
    .append("  value jsonb NOT NULL,").ln()
    .append("  PRIMARY KEY (branch_id),").ln()
    .append("  UNIQUE (doc_id, branch_name)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getDocBranch()).append("_DOC_DOC_ID_INDEX")
    .append(" ON ").append(options.getDocBranch()).append(" (doc_id);").ln()

    .append("CREATE INDEX ").append(options.getDocBranch()).append("_DOC_BRANCH_NAME_INDEX")
    .append(" ON ").append(options.getDocBranch()).append(" (branch_name);").ln()
    
    .append("CREATE INDEX ").append(options.getDocBranch()).append("_DOC_COMMIT_ID_INDEX")
    .append(" ON ").append(options.getDocBranch()).append(" (commit_id);").ln()
    
    .build()).build();
  }
  
  @Override
  public Sql createDocCommits() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDocCommits()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  branch_id VARCHAR(40) NOT NULL,").ln()
    .append("  doc_id VARCHAR(40) NOT NULL,").ln()
    .append("  datetime VARCHAR(29) NOT NULL,").ln()
    .append("  author VARCHAR(40) NOT NULL,").ln()
    .append("  message VARCHAR(255) NOT NULL,").ln()
    .append("  parent VARCHAR(40)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getDocCommits()).append("_DOC_COMMIT_DOC_ID_INDEX")
    .append(" ON ").append(options.getDocCommits()).append(" (doc_id);").ln()
    
    .append("CREATE INDEX ").append(options.getDocCommits()).append("_DOC_COMMIT_PARENT_INDEX")
    .append(" ON ").append(options.getDocCommits()).append(" (parent);").ln()
    
    .append("CREATE INDEX ").append(options.getDocCommits()).append("_DOC_COMMIT_BRANCH_ID_INDEX")
    .append(" ON ").append(options.getDocCommits()).append(" (branch_id);").ln()

     // internal foreign key
    .append("ALTER TABLE ").append(options.getDocCommits()).ln()
    .append("  ADD CONSTRAINT ").append(options.getDocCommits()).append("_DOC_COMMIT_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent)").ln()
    .append("  REFERENCES ").append(options.getDocCommits()).append(" (id);").ln().ln()
    
    .build()).build();
  }
  
  @Override
  public Sql createDocLog() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDocLog()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  value jsonb NOT NULL").ln()
    .append(");").ln()
    

    .append("CREATE INDEX ").append(options.getDocLog()).append("_DOC_LOG_COMMIT_ID_INDEX")
    .append(" ON ").append(options.getDocLog()).append(" (commit_id);").ln()
    
    .build()).build();
  }

  @Override
  public Sql createDocCommitsConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getDocCommits()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDocCommits()).append("_DOC_COMMIT_FK").ln()
        .append("  FOREIGN KEY (doc_id)").ln()
        .append("  REFERENCES ").append(options.getDoc()).append(" (id);").ln().ln()
        
//        .append("ALTER TABLE ").append(options.getDocCommits()).ln()
//        .append("  ADD CONSTRAINT ").append(options.getDocCommits()).append("_BRANCH_ID_FK").ln()
//        .append("  FOREIGN KEY (branch_id)").ln()
//        .append("  REFERENCES ").append(options.getDocBranch()).append(" (branch_id);").ln().ln()
            
        .build())
        .build();
  }

  @Override
  public Sql createDocLogConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getDocLog()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDocLog()).append("_DOC_LOG_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit_id)").ln()
        .append("  REFERENCES ").append(options.getDocCommits()).append(" (id);").ln().ln()
        
        .build())
        .build();
  }
  

  @Override
  public Sql createDocBranchConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getDocBranch()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDocBranch()).append("_DOC_ID_FK").ln()
        .append("  FOREIGN KEY (doc_id)").ln()
        .append("  REFERENCES ").append(options.getDoc()).append(" (id);").ln().ln()
        .build())
        .build();
  }

  @Override
  public Sql createOrgRights() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgRights()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  right_name VARCHAR(255) UNIQUE NOT NULL,").ln()
    .append("  right_description VARCHAR(255) NOT NULL").ln()
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getOrgRights()).append("_NAME_INDEX")
    .append(" ON ").append(options.getOrgRights()).append(" (right_name);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgRights()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgRights()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgRights()).append("_EXTERNAL_INDEX")
    .append(" ON ").append(options.getOrgRights()).append(" (external_id);").ln()
    

    .build()).build();
  }

  @Override
  public Sql createOrgParties() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgParties()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  party_name VARCHAR(255) UNIQUE NOT NULL,").ln()
    .append("  party_description VARCHAR(255) NOT NULL").ln()
    .append(");").ln().ln()
    
    // parent id, references self
    .append("ALTER TABLE ").append(options.getOrgParties()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgParties()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getOrgParties()).append(" (id);").ln()

    
    .append("CREATE INDEX ").append(options.getOrgParties()).append("_NAME_INDEX")
    .append(" ON ").append(options.getOrgParties()).append(" (party_name);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgParties()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgParties()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgParties()).append("_EXTERNAL_INDEX")
    .append(" ON ").append(options.getOrgParties()).append(" (external_id);").ln()
    
    
    .build()).build();
  }

  @Override
  public Sql createOrgPartyRights() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgPartyRights()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  party_id VARCHAR(40) NOT NULL,").ln()
    .append("  right_id VARCHAR(40) NOT NULL,").ln()
    .append("  UNIQUE (right_id, party_id)").ln()
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getOrgPartyRights()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgPartyRights()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgPartyRights()).append("_PARTY_INDEX")
    .append(" ON ").append(options.getOrgPartyRights()).append(" (party_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgPartyRights()).append("_RIGHT_INDEX")
    .append(" ON ").append(options.getOrgPartyRights()).append(" (right_id);").ln()    

    .build()).build();
  }

  @Override
  public Sql createOrgMembers() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgMembers()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  username VARCHAR(255) UNIQUE NOT NULL,").ln()
    .append("  email VARCHAR(255) NOT NULL").ln()
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getOrgMembers()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgMembers()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgMembers()).append("_EXTERNAL_INDEX")
    .append(" ON ").append(options.getOrgMembers()).append(" (external_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgMembers()).append("_MEMBER_NAME_INDEX")
    .append(" ON ").append(options.getOrgMembers()).append(" (username);").ln()

    .build()).build();
  }

  @Override
  public Sql createOrgMemberRights() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgMemberRights()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  member_id VARCHAR(40) NOT NULL,").ln()
    .append("  right_id VARCHAR(40) NOT NULL,").ln()
    .append("  party_id VARCHAR(40),").ln()
    .append("  UNIQUE NULLS NOT DISTINCT(member_id, right_id, party_id)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_RIGHT_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (right_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_MEMBER_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (member_id);").ln()
    

    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_PARTY_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (party_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_REF_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (right_id, member_id);").ln()    

    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_REF_2_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (right_id, member_id, party_id);").ln()    


    .build()).build();
  }

  @Override
  public Sql createOrgMemberships() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgMemberships()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  member_id VARCHAR(40) NOT NULL,").ln()
    .append("  party_id VARCHAR(40) NOT NULL,").ln()
    .append("  UNIQUE (member_id, party_id)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberships()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgMemberships()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberships()).append("_MEMBER_INDEX")
    .append(" ON ").append(options.getOrgMemberships()).append(" (member_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberships()).append("_PARTY_INDEX")
    .append(" ON ").append(options.getOrgMemberships()).append(" (party_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberships()).append("_REF_INDEX")
    .append(" ON ").append(options.getOrgMemberships()).append(" (party_id, member_id);").ln()    


    .build()).build();
  }

  @Override
  public Sql createOrgActorStatus() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgActorStatus()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  member_id VARCHAR(40),").ln()
    .append("  right_id VARCHAR(40),").ln()
    .append("  party_id VARCHAR(40),").ln()
    .append("  actor_status VARCHAR(100) NOT NULL,").ln() // visibility: in_force | archived 
    .append("  UNIQUE NULLS NOT DISTINCT(member_id, right_id, party_id)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_RIGHT_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (right_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_MEMBER_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (member_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_PARTY_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (party_id);").ln()
    

    .build()).build();
  }

  @Override
  public Sql createOrgCommits() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgCommits()).ln()
    .append("(").ln()
    .append("  commit_id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  created_at TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
    .append("  commit_log TEXT NOT NULL,").ln()
    
    .append("  commit_author VARCHAR(255) NOT NULL,").ln()
    .append("  commit_message VARCHAR(255) NOT NULL").ln()
    .append(");").ln().ln()
    
    
    .append("CREATE TABLE ").append(options.getOrgCommitTrees()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  parent_commit_id VARCHAR(40),").ln()
    .append("  actor_id VARCHAR(40) NOT NULL,").ln()
    .append("  actor_type VARCHAR(40) NOT NULL,").ln()
    .append("  value JSONB NOT NULL").ln()
    .append(");").ln().ln()
    

    // parent id, references self
    .append("ALTER TABLE ").append(options.getOrgCommits()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgCommits()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgCommits()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getOrgCommits()).append(" (parent_id);").ln()


    .append("ALTER TABLE ").append(options.getOrgCommitTrees()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgCommitTrees()).append("_COMMIT_FK").ln()
    .append("  FOREIGN KEY (commit_id)").ln()
    .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln()

    .append("ALTER TABLE ").append(options.getOrgCommitTrees()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgCommitTrees()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_commit_id)").ln()
    .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln()

    
    .append("CREATE INDEX ").append(options.getOrgCommitTrees()).append("_ACTOR_INDEX")
    .append(" ON ").append(options.getOrgCommitTrees()).append(" (actor_type, actor_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgCommitTrees()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgCommitTrees()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgCommitTrees()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getOrgCommitTrees()).append(" (parent_commit_id);").ln()
    

    .build()).build();
  }

  @Override
  public Sql createOrgActorData() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgActorData()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  member_id VARCHAR(40),").ln()
    .append("  right_id VARCHAR(40),").ln()
    .append("  party_id VARCHAR(40),").ln()

    .append("  data_type VARCHAR(255) NOT NULL,").ln()
    .append("  value JSONB NOT NULL,").ln()

    .append("  commit_author VARCHAR(255) NOT NULL,").ln()
    .append("  commit_message VARCHAR(255) NOT NULL").ln()
    .append(");").ln().ln()


    // parent id, references self
    .append("ALTER TABLE ").append(options.getOrgActorData()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgActorData()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getOrgActorData()).append(" (id);").ln()


    .build()).build();
  }

  @Override
  public Sql createOrgCommitConstraints() {
    return ImmutableSql.builder()
        .value(createOrgCommitFk(options.getOrgMemberRights()))
        .value(createOrgCommitFk(options.getOrgPartyRights()))
        .value(createOrgCommitFk(options.getOrgPartyRights()))
        
        .value(createOrgCommitFk(options.getOrgMembers()))
        .value(createOrgCommitFk(options.getOrgMemberRights()))
        
        .value(createOrgCommitFk(options.getOrgRights()))
        .value(createOrgCommitFk(options.getOrgMemberships()))
        .build();
  }

  @Override
  public Sql createOrgRightsConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append(createOrgRoleFk(options.getOrgActorData())).ln()
        .append(createOrgRoleFk(options.getOrgActorStatus())).ln()
        
        .append(createOrgRoleFk(options.getOrgMemberRights())).ln()
        .append(createOrgRoleFk(options.getOrgPartyRights())).ln()
        .build())
        .build();
  }
  
  @Override
  public Sql createOrgPartyConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append(createOrgGroupFk(options.getOrgActorData())).ln()
        .append(createOrgGroupFk(options.getOrgActorStatus())).ln()
        .append(createOrgGroupFk(options.getOrgPartyRights())).ln()
        .append(createOrgGroupFk(options.getOrgMemberRights())).ln()
        .append(createOrgGroupFk(options.getOrgMemberships())).ln()
        

        .append("ALTER TABLE ").append(options.getOrgMemberRights()).ln()
        .append("  ADD CONSTRAINT ").append(options.getOrgMemberRights()).append("_PARTY_MEMBER_FK").ln()
        .append("  FOREIGN KEY (party_id, member_id)").ln()
        .append("  REFERENCES ").append(options.getOrgMemberships()).append(" (party_id, member_id);").ln().ln()
        
        
        .build())
        .build();
  }

  
  @Override
  public Sql createOrgMemberConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append(createOrgUserFk(options.getOrgMemberships())).ln()
        .append(createOrgUserFk(options.getOrgMemberRights())).ln()
        
        .append(createOrgUserFk(options.getOrgActorData())).ln()
        .append(createOrgUserFk(options.getOrgActorStatus())).ln()
        .build())
        .build();
  }
  
  
  @Override public Sql dropRepo() { return dropTableIfNotExists(options.getTenant()); }
  @Override public Sql dropDoc() { return dropTableIfNotExists(options.getDoc()); }
  @Override public Sql dropDocBranch() { return dropTableIfNotExists(options.getDocBranch()); }
  @Override public Sql dropDocCommit() { return dropTableIfNotExists(options.getDocCommits()); }
  @Override public Sql dropDocLog() { return dropTableIfNotExists(options.getDocLog()); }
  

  @Override public Sql dropOrgRights() { return dropTableIfNotExists(options.getOrgRights()); }
  @Override public Sql dropOrgParties() { return dropTableIfNotExists(options.getOrgParties()); }
  @Override public Sql dropOrgPartyRights() { return dropTableIfNotExists(options.getOrgPartyRights()); }
  @Override public Sql dropOrgMembers() { return dropTableIfNotExists(options.getOrgMembers()); }
  @Override public Sql dropOrgMemberRights() { return dropTableIfNotExists(options.getOrgMemberRights()); }
  @Override public Sql dropOrgMemberships() { return dropTableIfNotExists(options.getOrgMemberships()); }
  @Override public Sql dropOrgActorStatus() { return dropTableIfNotExists(options.getOrgActorStatus()); }
  @Override public Sql dropOrgActorLogs() { return dropTableIfNotExists(options.getOrgCommits()); }
  @Override public Sql dropOrgActorData() { return dropTableIfNotExists(options.getOrgActorData()); }

  
  
  private Sql dropTableIfNotExists(String tableName) {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(tableName).append(";").ln()
        .build()).build();
  }
  
  private String createOrgCommitFk(String tableNameThatPointToCommits) {
    return  new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit_id)").ln()
        .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln().ln()
        .build();
  }

  private String createOrgUserFk(String tableNameThatPointToCommits) {
    return  new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_MEMBER_FK").ln()
        .append("  FOREIGN KEY (member_id)").ln()
        .append("  REFERENCES ").append(options.getOrgMembers()).append(" (id);").ln().ln()
        .build();
  }

  private String createOrgGroupFk(String tableNameThatPointToCommits) {
    return new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_PARTY_FK").ln()
        .append("  FOREIGN KEY (party_id)").ln()
        .append("  REFERENCES ").append(options.getOrgParties()).append(" (id);").ln().ln()
        .build();
  }
  

  private String createOrgRoleFk(String tableNameThatPointToCommits) {
    return  new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_RIGHT_FK").ln()
        .append("  FOREIGN KEY (right_id)").ln()
        .append("  REFERENCES ").append(options.getOrgRights()).append(" (id);").ln().ln()
        .build();
  }
}
