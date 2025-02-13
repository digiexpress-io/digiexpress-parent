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
import java.util.Collection;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "feedback_category", 
  uniqueConstraints = {
      @UniqueConstraint(columnNames = {"label", "sub_label", "origin"})
  }
)
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
@Getter
@Setter
@Accessors(chain=true)
public class FeedbackCategoryEntity {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
  @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
  private UUID id;
  
  @Column(name="label", nullable = false)
  private String label;                 // A selection from a dialob or a stencil article
  
  @Column(name="sub_label")
  private String subLabel;              // A child selection from a dialob or stencil article 
  
  @Column(name="origin", nullable = false, updatable = false)
  private String origin;                // Can be related to a customer question, or a stencil article
  
  @Column(name="created_by_user_id", nullable = false, updatable = false)
  private String createdByUserId;       // The user who wrote and approved the feedback
  
  @Column(name="created_on_date", nullable = false, updatable = false)
  private ZonedDateTime createdOnDate;

  @Column(name="updated_on_date", nullable = false)
  private ZonedDateTime updatedOnDate;
  
  
  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval = true)
  @JsonBackReference
  private Collection<FeedbackReplyEntity> replies;

  
  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval = true)
  @JsonBackReference
  private Collection<FeedbackApprovalEntity> approvals;
}
