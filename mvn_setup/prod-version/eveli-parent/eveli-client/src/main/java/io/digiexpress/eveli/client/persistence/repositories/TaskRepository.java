package io.digiexpress.eveli.client.persistence.repositories;

/*-
 * #%L
 * eveli-client
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

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.digiexpress.eveli.client.persistence.entities.TaskEntity;

@Repository
public interface TaskRepository extends PagingAndSortingRepository<TaskEntity, Long> {
  
  TaskEntity getOneById(Long id);
  
  @Query(value=
      "select distinct t from TaskEntity t join t.assignedRoles r left join t.assignedRoles r2 where " +
      " (lower(subject) like :subject or lower(taskRef) like :subject)" +
      " and lower(coalesce(clientIdentificator, '')) like :clientIdentificator" +
      " and lower(coalesce(assignedUser, '')) like :assignedUser" +
      " and priority in :priority" +
      " and status in :status" +
      " and lower(coalesce(r2, '')) like :searchRole" +
      " and r in :roles" +
      " and (:dueDate is null or t.dueDate < CURRENT_DATE)")
  Page<TaskEntity> searchTasks(
      @Param("subject") String subject, 
      @Param("clientIdentificator") String clientIdentificator, 
      @Param("assignedUser") String assignedUser, 
      @Param("status") List<Integer> status,
      @Param("priority") List<Integer> priority,
      @Param("roles") List<String> roles,
      @Param("searchRole") String searchRole,
      @Param("dueDate") String dueDate,
      Pageable page);
  
  @Query(value=
      "select distinct t from TaskEntity t left join t.assignedRoles r2 where " +
      " (lower(subject) like :subject or lower(taskRef) like :subject)" +
      " and lower(coalesce(clientIdentificator, '')) like :clientIdentificator" +
      " and lower(coalesce(assignedUser, '')) like :assignedUser" +
      " and priority in :priority" +
      " and status in :status" +
      " and lower(coalesce(r2, '')) like :searchRole" +
      " and (:dueDate is null or t.dueDate < CURRENT_DATE)")
  Page<TaskEntity> searchTasksAdmin(
      @Param("subject") String subject,  
      @Param("clientIdentificator") String clientIdentificator,
      @Param("assignedUser") String assignedUser,
      @Param("status") List<Integer> status,
      @Param("priority") List<Integer> priority,
      @Param("searchRole") String searchRole,
      @Param("dueDate") String dueDate,
      Pageable page);

  
  @Query(value="select task from TaskEntity as task where id in :ids")
  List<TaskEntity> findAllTasksId(@Param("ids") List<Long> ids);
  
  
  @Query(value="select distinct t.id from task t join comment c on c.task_id = t.id left join task_access ta on t.id = ta.task_id and ta.user_id=:user_id where (ta.task_id is null or c.created > ta.updated) and c.external=TRUE", nativeQuery = true)
  List<Long> findUnreadExternalTasks(@Param("user_id") String userName);
  
  @Query(value="select distinct t.id from task t left join task_access ta on t.id = ta.task_id and ta.user_id=:user_id left join comment c on c.task_id = t.id where (ta.task_id is null or c.created > ta.updated)", nativeQuery = true)
  List<Long> findUnreadTasks(@Param("user_id") String userName);
  
  @Query(value="select distinct t.id from task t inner join task_roles ar on t.id =ar.task_id left join task_access ta on t.id = ta.task_id and ta.user_id=:user_id left join comment c on c.task_id = t.id where (ta.task_id is null or c.created > ta.updated) and ar.assigned_roles in :roles", nativeQuery = true)
  List<Long> findUnreadTasksByRole(@Param("user_id") String userName, @Param("roles") List<String> roles);


  TaskEntity save(TaskEntity task);
  
  /*
  @Query(value="select new io.digiexpress.task.entity.TaskStatusStatistics(count(status), status) from Task t group by status")
  List<TaskStatusStatistics> getTaskStatusStatistics();
  
  @Query(value="select new io.digiexpress.task.entity.TaskPriorityStatistics(count(priority), priority) from Task t group by priority")
  List<TaskPriorityStatistics> getTaskPriorityStatistics();
  
  @Query(nativeQuery = true, value="with t as (select date(created) as statusdate, 0 as status from task  union all select date(updated) \n"
      + "as statusdate, status from task where status <>0) \n"
      + "select statusdate, \n"
      + "count(status) filter (where status=0) as \"NEW\", \n"
      + "count(status) filter (where status=1) as \"OPEN\", \n"
      + "count(status) filter (where status=2) as \"COMPLETED\", \n"
      + "count(status) filter (where status=3) as \"REJECTED\"\n"
      + "from t group by statusdate order by statusdate")
  List<TaskStatusTimelineStatistics> getTaskStatusTimelineStatistics();
  */
}
