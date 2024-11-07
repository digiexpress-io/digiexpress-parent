package io.digiexpress.eveli.client.persistence.entities;

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

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.digiexpress.eveli.client.api.ProcessClient.ProcessStatus;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name="process")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
@Getter
@Setter
@Accessors(chain=true)
public class ProcessEntity {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
	private Long id;

  @Column(name="workflow_name", nullable=false)
  private String workflowName;
  
  @Column(name="status")
  @Enumerated(EnumType.STRING)
	private ProcessStatus status;
	
  @Column(name="questionnaire_id")
  private String questionnaire;
  
  @Column(name="task_id")
  private String task;
  
  @Column(name="input_context_id")
  private String inputContextId;
  
  @Column(name="input_parent_context_id")
  private String inputParentContextId;
  
  @Column(name="user_id")
  private String userId;
  
  @Column(name="created", nullable = false, updatable = false)
  private LocalDateTime created;

  @Column(name="updated", nullable = false)
  private LocalDateTime updated;

  @Basic(fetch = FetchType.LAZY)
  @Column(name = "form_body")
  @JdbcTypeCode(SqlTypes.JSON)
  private String formBody;

  @Basic(fetch = FetchType.LAZY)
  @Column(name = "flow_body")
  @JdbcTypeCode(SqlTypes.JSON)
  private String flowBody;

  
  @PrePersist
  void prePersist() {
    updated = LocalDateTime.now();
    if (id == null) {
      created = updated;
    }
  }

  @PreUpdate
  void preUpdated() {
    updated = LocalDateTime.now();
  }
}
