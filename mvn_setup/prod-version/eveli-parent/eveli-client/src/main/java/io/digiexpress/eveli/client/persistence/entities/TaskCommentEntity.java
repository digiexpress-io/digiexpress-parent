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

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Entity
@Table(name="comment")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class TaskCommentEntity {
  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="task_id", foreignKey = @ForeignKey(name = "fk_comment_task_id_to_task"))
  @JsonBackReference
  @ToString.Exclude
  private TaskEntity task;

  @Column(nullable = false)
  private ZonedDateTime created;

  @ManyToOne(fetch=FetchType.EAGER)
  @JoinColumn(name="reply_to_id", foreignKey = @ForeignKey(name = "fk_comment_reply_to_id_to_comment"))
  private TaskCommentEntity replyTo;

  @Column(name="comment_text", columnDefinition = "TEXT", nullable = false)
  private String commentText;

  @Column(name="user_name")
  private String userName;
  
  @Column(name="external")
  private Boolean external;
  
  @Column(name="source")
  @Enumerated(EnumType.STRING)
  private TaskCommentSource source; 
  
  @PrePersist
  void updateTimestamp() {
    if (created == null) {
      created = ZonedDateTime.now(ZoneId.of("UTC"));
    }
  }

}

