package io.resys.thena.docdb.test.fs;

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

import java.io.Serializable;
import java.time.Duration;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.fs.FsDirent.DirentType;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirent;
import io.resys.thena.docdb.test.config.DbTestTemplate;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SimpleFsUpdateDirentTest extends DbTestTemplate {

  @Value.Immutable
  public interface TestContent extends Serializable {
    String getId();
    String getName();
  }
  
  private FsDirentContainer createDirent(CreatedTenant repo) {
    return getClient().fs(repo).commit()
        .createManyDirents()
        .commitMessage("batching tests")
        .commitAuthor("jane.doe@folders.com")
        .addDirent((NewDirent newMission) -> {
          
          newMission
            .direntName("my first file")
            .direntType(DirentType.FILE)
            .externalId("file-1")
            
            .addLabels(newLabel -> newLabel.labelType("keyword").labelValue("housing").build())
            .addLabels(newLabel -> newLabel.labelType("keyword").labelValue("roofing").build())
            
            .addAssignees(newAssignee -> newAssignee.assignmentType("worker").assignee("sam-from-the-mill").build())
            .addAssignees(newAssignee -> newAssignee.assignmentType("worker").assignee("jane-from-the-roofing").build())
            
            .addLink(newLink -> newLink.linkType("project-plans").linkValue("site.com/plans/1").build())
            .addLink(newLink -> newLink.linkType("permits").linkValue("site.com/permits/5").build())
            
            .addRemark(newRemark -> newRemark.remarkText("Created main task for building a house!").reporterId("jane.doe").build())
            .addRemark(newRemark -> newRemark.remarkText("Waiting for results already!").reporterId("the.bob.clown").build())
            
            .build();

        }).build()
        .onItem().transformToUni(resp -> {
          final var missionId = resp.getDirents().iterator().next().getId();
          return getClient().fs(repo).find().direntQuery().get(missionId);
        })
        .onItem().transform(resp -> resp.getObjects())
        .await().atMost(Duration.ofMinutes(1));
  }

  @Test
  public void createAndUpdateDirent() {
    // create project
    CreatedTenant repo = getClient().tenants().createOneTenant()
        .name("SimpleFsUpdateDirentTest-1", StructureType.fs)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    
    final var newMission = createDirent(repo).getDirents().values().iterator().next();
    
    // add comments
    getClient().fs(repo).commit().modifyManyDirents()
    .commitMessage("forgot to add comments to things")
    .commitAuthor("jane.doe@folders.com")
    .modifyDirent(newMission.getId(), (modifyMission) -> {
      
      modifyMission.direntName("renamed-better-name")
      
      .addRemark((newRemark) -> 
        newRemark
        .remarkText("Not to self, give feedback to architects")
        .reporterId("jane.doe@folders.com")
        .build())
      .addRemark((newRemark) -> 
        newRemark
        .remarkText("Note to self, compliment works on after job well done!")
        .reporterId("jane.doe@folders.com")
        .build())
      .build();
      
    })
    .build()
    .await().atMost(Duration.ofMinutes(1));
        
    
    // modify title
    getClient().fs(repo).commit().modifyManyDirents()
    .commitMessage("changed the title")
    .commitAuthor("jane.doe@folders.com")
    .modifyDirent(newMission.getId(), (modifyMission) -> {
      
      modifyMission
      .direntName("House plans for customer #198CC")
      .direntDescription("Basic house plans for customer")
      .build();
      
    })
    .build()
    .await().atMost(Duration.ofMinutes(1));
    

        
    
    Assertions.assertEquals(
"""

Repo
  - id: 1, rev: 2
    name: SimpleFsUpdateDirentTest-1, prefix: 3, type: fs
Dirent: 5
  - 12::FS_DIRENT_REMARK
  - 13::FS_DIRENT_REMARK
  - 14::FS_DIRENT_REMARK
  - 15::FS_DIRENT_REMARK
  - 16::FS_DIRENT_ASSIGNMENT
  - 17::FS_DIRENT_ASSIGNMENT
  - 18::FS_DIRENT_LABEL
  - 19::FS_DIRENT_LABEL
  - 20::FS_DIRENT_LINKS
  - 21::FS_DIRENT_LINKS

commit: 4, tenant: 1
author: jane.doe@folders.com, message: batching tests
 | created
  + added new: 9 entries
  + 18::FS_DIRENT_LABEL
    {"docType":"FS_DIRENT_LABEL","id":"18","commitId":"4","labelType":"keyword","labelValue":"housing","labelBody":null,"direntId":"5"}
  + 19::FS_DIRENT_LABEL
    {"docType":"FS_DIRENT_LABEL","id":"19","commitId":"4","labelType":"keyword","labelValue":"roofing","labelBody":null,"direntId":"5"}
  + 17::FS_DIRENT_ASSIGNMENT
    {"docType":"FS_DIRENT_ASSIGNMENT","id":"17","commitId":"4","direntId":"5","assignee":"sam-from-the-mill","assignmentType":"worker","assigneeContact":null}
  + 16::FS_DIRENT_ASSIGNMENT
    {"docType":"FS_DIRENT_ASSIGNMENT","id":"16","commitId":"4","direntId":"5","assignee":"jane-from-the-roofing","assignmentType":"worker","assigneeContact":null}
  + 21::FS_DIRENT_LINKS
    {"docType":"FS_DIRENT_LINKS","id":"21","commitId":"4","createdWithCommitId":"4","direntId":"5","linkValue":"site.com/plans/1","linkType":"project-plans","linkBody":null}
  + 20::FS_DIRENT_LINKS
    {"docType":"FS_DIRENT_LINKS","id":"20","commitId":"4","createdWithCommitId":"4","direntId":"5","linkValue":"site.com/permits/5","linkType":"permits","linkBody":null}
  + 12::FS_DIRENT_REMARK
    {"docType":"FS_DIRENT_REMARK","id":"12","commitId":"4","createdWithCommitId":"4","direntId":"5","parentId":null,"remarkText":"Created main task for building a house!","reporterId":"jane.doe","remarkStatus":null,"remarkType":null,"remarkSource":null}
  + 13::FS_DIRENT_REMARK
    {"docType":"FS_DIRENT_REMARK","id":"13","commitId":"4","createdWithCommitId":"4","direntId":"5","parentId":null,"remarkText":"Waiting for results already!","reporterId":"the.bob.clown","remarkStatus":null,"remarkType":null,"remarkSource":null}
  + 5::FS_DIRENT
    {"docType":"FS_DIRENT","id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","externalId":"file-1","archivedAt":null,"archivedStatus":null,"direntParentId":null,"direntRef":"11","direntType":"FILE","direntName":"my first file","direntDescription":"","direntUserType":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 0 entries


commit: 7, tenant: 1
author: jane.doe@folders.com, message: forgot to add comments to things
 | created
  + added new: 2 entries
  + 14::FS_DIRENT_REMARK
    {"docType":"FS_DIRENT_REMARK","id":"14","commitId":"7","createdWithCommitId":"7","direntId":"5","parentId":null,"remarkText":"Not to self, give feedback to architects","reporterId":"jane.doe@folders.com","remarkStatus":null,"remarkType":null,"remarkSource":null}
  + 15::FS_DIRENT_REMARK
    {"docType":"FS_DIRENT_REMARK","id":"15","commitId":"7","createdWithCommitId":"7","direntId":"5","parentId":null,"remarkText":"Note to self, compliment works on after job well done!","reporterId":"jane.doe@folders.com","remarkStatus":null,"remarkType":null,"remarkSource":null}

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 1 entries
  +- 5::FS_DIRENT
   -  {"docType":"FS_DIRENT","id":"5","commitId":"4","createdWithCommitId":"4","updatedTreeWithCommitId":"4","externalId":"file-1","archivedAt":null,"archivedStatus":null,"direntParentId":null,"direntRef":"11","direntType":"FILE","direntName":"my first file","direntDescription":"","direntUserType":null}
   +  {"docType":"FS_DIRENT","id":"5","commitId":"7","createdWithCommitId":"4","updatedTreeWithCommitId":"7","externalId":"file-1","archivedAt":null,"archivedStatus":null,"direntParentId":null,"direntRef":"11","direntType":"FILE","direntName":"renamed-better-name","direntDescription":"","direntUserType":null}
   diff: updatedTreeWithCommitId :: 4 -> 7
   diff: direntName :: my first file -> renamed-better-name


commit: 9, tenant: 1
author: jane.doe@folders.com, message: changed the title
 | created
  + added new: 0 entries

 | deleted
  - deleted: 0 entries

 | merged
  +- merged: 1 entries
  +- 5::FS_DIRENT
   -  {"docType":"FS_DIRENT","id":"5","commitId":"7","createdWithCommitId":"4","updatedTreeWithCommitId":"7","externalId":"file-1","archivedAt":null,"archivedStatus":null,"direntParentId":null,"direntRef":"11","direntType":"FILE","direntName":"renamed-better-name","direntDescription":"","direntUserType":null}
   +  {"docType":"FS_DIRENT","id":"5","commitId":"9","createdWithCommitId":"4","updatedTreeWithCommitId":"9","externalId":"file-1","archivedAt":null,"archivedStatus":null,"direntParentId":null,"direntRef":"11","direntType":"FILE","direntName":"House plans for customer #198CC","direntDescription":"Basic house plans for customer","direntUserType":null}
   diff: updatedTreeWithCommitId :: 7 -> 9
   diff: direntName :: renamed-better-name -> House plans for customer #198CC
   diff: direntDescription ::  -> Basic house plans for customer

""", toStaticData(repo.getRepo()));
  }
}
