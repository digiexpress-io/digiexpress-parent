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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Entity
@Table(name="task")
@Audited
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class TaskEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @Column(name = "task_ref", unique = true, nullable = false, updatable = false)
  private String taskRef;

  @Version
  @Column(nullable = false)
  private Integer version;

  @Column(nullable = false, updatable = false)
  private ZonedDateTime created;

  @Column(nullable = false)
  private ZonedDateTime updated;

  @Column
  private ZonedDateTime completed;
  
  // client name and/or ID for search purposes.
  @Column(name="client_identificator")
  private String clientIdentificator;

  @ElementCollection
  @CollectionTable(
    name = "task_roles",
    joinColumns = @JoinColumn(name = "task_id"),
    foreignKey = @ForeignKey(name = "fk_task_roles_task_id_to_task")
  )
  @Column(name="assigned_roles")
  private Set<String> assignedRoles;
  
  @Column(name = "assigned_user")
  private String assignedUser;

  @Column(name = "assigned_user_email")
  private String assignedUserEmail;
  
  @Column(name = "updater_id")
  private String updaterId;

  @Column(name = "questionnanire_id")
  private String questionnanireId;
  
  @Column(name="due_date")
  private LocalDate dueDate;

  @Column(nullable = false)
  private TaskStatus status;


  @Column(nullable = false)
  private String subject;

  @Column(columnDefinition="TEXT")
  private String description;

  @Column(nullable = false)
  private TaskPriority priority;

  @ElementCollection
  @CollectionTable(
    name = "task_keywords",
    joinColumns = @JoinColumn(name = "task_id"),
    foreignKey = @ForeignKey(name = "fk_task_keywords_task_id_to_task")
  )
  @Column(name="key_words")
  @NotAudited
  private Set<String> keyWords;

  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
  @JsonBackReference
  @NotAudited
  private Collection<TaskCommentEntity> comments;
  
  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
  @JsonBackReference
  @NotAudited
  private Collection<TaskPayloadEntity> payloads;
  
  
  @PrePersist
  void prePersist() {
    updated = ZonedDateTime.now(ZoneId.of("UTC"));
    if (id == null) {
      created = updated;
    }
    if (status == null) {
      status = TaskStatus.NEW;
    }
    if (priority == null) {
      priority = TaskPriority.NORMAL;
    }
  }

  @PreUpdate
  void preUpdated() {
    updated = ZonedDateTime.now(ZoneId.of("UTC"));
  }


  public void setKeyWords(Collection<String> keyWords) {
    this.keyWords = keyWords != null ? new HashSet<>(keyWords): null;
  }
}
