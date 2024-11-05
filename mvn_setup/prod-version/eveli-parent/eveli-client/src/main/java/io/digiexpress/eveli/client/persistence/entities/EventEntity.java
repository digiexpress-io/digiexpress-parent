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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Entity
@Table(name="task_process_event")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class EventEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "target_id", nullable = false)
  private String targetId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private EventStatus status;
  
  @Column(name = "created_date", nullable = false)
  private ZonedDateTime created;
  
  @Column(name = "updated_date")
  private ZonedDateTime updated;

  @Column(name = "event_type")
  @Enumerated(EnumType.STRING)
  private EvenType eventType;
  
  @Column(name = "event_body", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private String eventBody;
  
  @Column(name = "error_text")
  private String errorText;
  
  public enum EventStatus {
    DONE, FAILED, NEW
  }
  
  public enum EvenType {
    TASK_EVENT, 
  }
  
  @PrePersist
  void prePersist() {
    updated = ZonedDateTime.now(ZoneId.of("UTC"));
    if (id == null) {
      created = updated;
    }
    if (status == null) {
      status = EventStatus.NEW;
    }
  }

}
