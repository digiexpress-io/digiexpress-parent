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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity
@Table(name="feedback_approval", 
uniqueConstraints = {
    @UniqueConstraint(columnNames = {"category_id", "reply_id", "source_id"})
})
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
@Getter
@Setter
@Accessors(chain=true)
public class FeedbackApprovalEntity {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
  @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
  @Convert(converter = HibernateUUIDConverter.class)
  private String id;
  
  @Column(name="category_id", columnDefinition = "UUID", nullable = false, insertable = false, updatable = false)
  @Convert(converter = HibernateUUIDConverter.class)
  private String categoryId;

  @Column(name="reply_id", columnDefinition = "UUID", nullable = true, insertable = false, updatable = false)
  @Convert(converter = HibernateUUIDConverter.class)
  private String replyId;
  
  @Column(name="source_id", nullable = false, updatable = false)
  private String sourceId;       // external id that identifies the user in GDPR-friendly manner. Cannot contain any personal data.
  
  @Column(name="star_rating", nullable = false)
  private Integer starRating;    // rating from 1-5: 1 = thumbs down, 5 = thumbs up
  
  @Column(name="created_on_date", nullable = false, updatable = false)
  private ZonedDateTime createdOnDate;

  @Column(name="updated_on_date", nullable = false)
  private ZonedDateTime updatedOnDate;
  
  
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="reply_id", foreignKey = @ForeignKey(name = "fk_approval_to_reply"))
  @JsonBackReference
  @ToString.Exclude
  private FeedbackReplyEntity reply;
  
  
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="category_id", foreignKey = @ForeignKey(name = "fk_approval_to_category"))
  @JsonBackReference
  @ToString.Exclude
  private FeedbackCategoryEntity category;

}
