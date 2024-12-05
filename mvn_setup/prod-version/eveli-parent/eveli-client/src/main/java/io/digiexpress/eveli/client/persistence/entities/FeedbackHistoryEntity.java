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

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name="feedback_history")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
@Getter
@Setter
@Accessors(chain=true)
public class FeedbackHistoryEntity {
  
  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
  @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
  private String id;

  @Column(name="commit_id", nullable = false)
  private String commitId;
  
  @Column(name="rating_id", nullable = true)
  private String ratingId;
  
  @Column(name="category_id", nullable = true)
  private String categoryId;

  @Column(name="reply_id", nullable = true)
  private String replyId;


  @Column(name="json_body_type", nullable = false)
  private String jsonBodyType;
  
  @Column(name="json_body", nullable = false, columnDefinition = "JSONB")
  private String jsonBody;
  
  @Column(name="created_on_date", nullable = false, updatable = false)
  private ZonedDateTime createdOnDate;
  
  @Column(name="created_by", nullable = false)
  private String createdBy;
}
