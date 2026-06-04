package io.resys.limaone.fs;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.limaone.model.Description;
import io.resys.limaone.model.Model.BodyType;
import jakarta.annotation.Nullable;



public interface WorldFsProps {
  String getId();
  BodyType getType();
  Boolean getLocked();
  
  @Nullable String getDescription();
  List<Description.DescriptionLabel> getLabels();
  
  
  List<ConfigOption> getConfigOptions();
  List<Comment> getComments();
  List<Change> getChanges();
  List<Permission> getPermissions();
  

  List<Error> getErrors();
  
  
  interface FolderProps extends WorldFsProps {

  }

  @Value.Immutable
  interface ArticleProps extends WorldFsProps {
    Integer getOrderNumber();

  }
  
  @Value.Immutable
  interface ArticlePageProps extends WorldFsProps {
    String getLocaleCode();
    String getArticleId();
    
    @Nullable 
    String getContent();
  }

  @Value.Immutable
  interface LinkProps extends WorldFsProps {
    String getUrlValue();
    Map<String, String> getIntlValues();
    List<String> getArticles();
    String getContentType();
  }
  
  interface TemplateProps extends WorldFsProps {
    String getPrintoutServiceId();
    String getLocaleId();
    
    @Nullable 
    String getContent();
  }
  
  @Value.Immutable
  interface ServiceProps extends WorldFsProps {

    String getServiceName();
    String getDialobFormName();
    String getDialobFormTag();
    String getFlowName();
    
    @Nullable 
    String getValidityStart();
    @Nullable 
    String getValidityEnd();
    
    List<String> getArticles();
    Map<String, String> getIntlValues();
  }

  @Value.Immutable
  interface DialobProps extends WorldFsProps {
    String getFormName();
    String getFormTechnicalId();
    
    @Nullable 
    List<String> getVersionTags();
  }

  @Value.Immutable
  interface FlowProps extends WorldFsProps {
    String getName();
    
    @Nullable 
    String getContent();
  }
  
  @Value.Immutable
  interface FlowTaskProps extends WorldFsProps {
    String getTaskName();
    String getTaskValue();
  }

  @Value.Immutable
  interface LocaleProps extends WorldFsProps {
    String getLocaleCode();
  }
  
  @Value.Immutable
  interface PrintoutProps extends WorldFsProps {
    String getPrintoutServiceName();
    String getOrchestratorName();
    Map<String, String> getIntlValues();
  }
  
  @Value.Immutable
  interface PrintoutPageProps extends WorldFsProps {
    String getContent(); // the markdown definition
    String getLocaleId();
    String getServiceId();
    List<String> getTemplateIds();
  }
  
  
  @Value.Immutable
  interface PrintoutResourceProps extends WorldFsProps {
    String getExternalLocation();
    String getResourceName();
    String getContentType();
    List<String> getPrintoutPageIds();

    @Nullable String getContent();
  }

  interface ImageProps extends WorldFsProps {

  }

  interface PhoneProps extends WorldFsProps {

    String getPhoneValue();
    Map<String, String> getIntlValues();
  }

  interface AssetError {
    String getCode();
    ErrorSeverityType getSeverity();
    String getMessage();
  }
  
  interface Change {
    ChangeType getChangeType();
    OffsetDateTime getChangeDate();;
    User getChangedBy();
  }
  
  interface User {
    String getUserName();
    String getEmail();
    List<PermissionType> getPermissions();
  }
  
  interface Permission {
    String getName();
    List<PermissionType> getTypes();
  }
  
  @Value.Immutable
  interface Comment {
    String getComment();
    String getAuthor();
    OffsetDateTime getCreated();
  }

  
  enum ChangeType { 
    UPDATE, 
    CREATE, 
    DELETE 
  }

  enum PermissionType {
    READ,
    WRITE,
    VIEW,
    NONE
  }

  enum ConfigOption {
    DEV_MODE,
    ASSIGNABLE_MODE,
    DISABLED_MODE,
    ANONYMOUS_MODE,
    AUTH_ONLY_MODE
  } 
  
  enum ErrorSeverityType {
    CRITICAL,
    WARNING
  }
 
}
