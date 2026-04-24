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

import io.resys.limaone.model.Model.BodyType;
import jakarta.annotation.Nullable;



public interface WorldFsProps {
  String getId();
  Boolean getExpanded();
  Boolean getReference();
  Boolean getLocked();
  
  @Nullable
  String getDescription();
  
  List<ConfigOption> getConfigOptions();
  List<Comment> getComments();
  List<Change> getChanges();
  List<Permission> getPermissions();
  
  List<Label> getLabels();
  List<Error> getErrors();
  
  
  interface FolderProps extends WorldFsProps {
    BodyType getType();
  }

  interface ArticleProps extends WorldFsProps {
    BodyType getType();
    Integer getOrderNumber();
  }

  interface ServiceProps extends WorldFsProps {
    BodyType getType();
    String getServiceName();
    String getDialobFormName();
    String getDialobFormTag();
    String getFlowName();
    
    @Nullable 
    String getValidityStart();
    @Nullable 
    String getValidityEnd();
    
    List<String> getArticles();
    List<ConfigOption> getConfigOptions();
    Map<String, String> getIntlValues();
  }

  interface DialobProps extends WorldFsProps {
    BodyType getType();
    String getFormName();
    String getFormTechnicalId();
    
    @Nullable 
    List<String> getVersionTags();
  }

  interface FlowProps extends WorldFsProps {
    BodyType getType();
    String getName();
    
    @Nullable 
    String getContent();
  }

  interface LanguageProps extends WorldFsProps {
    BodyType getType();
    String getLocaleCode();
  }

  interface PageProps extends WorldFsProps {
    BodyType getType();
    String getLocaleCode();
    String getArticleId();
    
    @Nullable 
    String getContent();
  }

  interface PrintoutProps extends WorldFsProps {
    BodyType getType();
    String getPrintoutServiceName();
    String getOrchestratorName();
    Map<String, String> getIntlValues();
  }

  interface ImageProps extends WorldFsProps {
    BodyType getType();
  }

  interface TemplateProps extends WorldFsProps {
    BodyType getType();
    String getPrintoutServiceId();
    String getLocaleId();
    
    @Nullable 
    String getContent();
  }

  interface LinkProps extends WorldFsProps {
    BodyType getType();
    String getUrlValue();
    Map<String, String> getIntlValues();
  }

  interface PhoneProps extends WorldFsProps {
    BodyType getType();
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
  
  public interface User {
    String getUserName();
    String getEmail();
    List<PermissionType> getPermissions();
  }
  
  public interface Permission {
    String getName();
    List<PermissionType> getTypes();
  }
  
  public interface Comment {
    String getComment();
    String getAuthor();
    OffsetDateTime getCreated();
  }

  public interface Label {
    String getId();
    String getValue();
  }
  
  public enum ChangeType { 
    UPDATE, 
    CREATE, 
    DELETE 
  }

  public enum PermissionType {
    READ,
    WRITE,
    VIEW,
    NONE
  }

  public enum ConfigOption {
    DEV_MODE,
    ASSIGNABLE_MODE,
    DISABLED_MODE,
    ANONYMOUS_MODE
  } 
  
  public enum ErrorSeverityType {
    CRITICAL,
    WARNING
  }
 
}
