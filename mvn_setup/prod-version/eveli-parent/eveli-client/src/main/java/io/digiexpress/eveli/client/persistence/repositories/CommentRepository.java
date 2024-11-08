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

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.digiexpress.eveli.client.persistence.entities.TaskCommentEntity;

@Repository
public interface CommentRepository extends CrudRepository<TaskCommentEntity, Long> {
	
  
  TaskCommentEntity getOneById(Long id);
	
	Collection<TaskCommentEntity> findByTaskId(Long id);
	Collection<TaskCommentEntity> findByTaskIdAndExternalTrue(Long id);
	
	
	@Query(nativeQuery = true, value=
"""
SELECT comment.* 
FROM comment
left join task on(task.id = comment.task_id)
left join process on(process.task_id::bigint = comment.task_id)
where comment.external = true and process.user_id = :userName
""")
  List<TaskCommentEntity> findAllByUserId(@Param("userName") String userName);
  
}
