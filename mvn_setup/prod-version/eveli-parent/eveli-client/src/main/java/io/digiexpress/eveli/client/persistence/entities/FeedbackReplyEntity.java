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

import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity
@Table(name="feedback_reply", uniqueConstraints = {
  @UniqueConstraint(columnNames = {"category_id", "source_id"})
})
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
@Getter
@Setter
@Accessors(chain=true)
public class FeedbackReplyEntity {
  
  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
  @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
  private UUID id;

  @Column(name="category_id", columnDefinition = "UUID", nullable = false, insertable = false, updatable = false)
  private UUID categoryId; 
  
  @Column(name="content", columnDefinition = "TEXT", nullable = false)
  private String content;                    // combined markdown from dialob and worker
  
  @Column(name="locale", nullable = false)
  private String locale;                     // intended language of the content
  
  @Column(name="localized_label",nullable = false)
  private String localizedLabel;             // localized label at the time of content creation
  
  @Column(name="source_id", nullable = true) // process id or task id, etc., from where this reply was captured
  private String sourceId;
  
  @Column(name="localized_sub_label", nullable = true)
  private String localizedSubLabel;
  
  @Column(name="created_on_date", nullable = false, updatable = false)
  private ZonedDateTime createdOnDate;

  @Column(name="updated_on_date", nullable = false)
  private ZonedDateTime updatedOnDate;

  @Column(name="updated_by", nullable = false)
  private String updatedBy;  
  
  @Column(name="created_by", nullable = false)
  private String createdBy;  
  
  @Column(name="reporter_names", nullable = true)
  private String reporterNames;  
  
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="category_id", foreignKey = @ForeignKey(name = "fk_reply_to_category"))
  @JsonBackReference
  @ToString.Exclude
  @NotAudited
  private FeedbackCategoryEntity category;
  
  @OneToMany(mappedBy = "reply", cascade = CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval = true)
  @JsonBackReference
  @NotAudited
  private Collection<FeedbackApprovalEntity> approvals;
  

}
