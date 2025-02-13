package io.thestencil.client.spi.beans;

/*-
 * #%L
 * stencil-client-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;

import io.thestencil.client.api.MigrationBuilder;

public final class TopicLinkBean implements MigrationBuilder.TopicLink {
  private String id;
  private String type;
  private String name;
  private String path;
  private String value;
  private Boolean global;
  private Boolean workflow;
  public TopicLinkBean(
      String id,
      String path,
      String type,
      String name,
      String value,
      Boolean global,
      Boolean workflow) {
    this.id = Objects.requireNonNull(id, "id");
    this.path = Objects.requireNonNull(path, "path");
    this.type = Objects.requireNonNull(type, "type");
    this.name = Objects.requireNonNull(name, "name");
    this.value = Objects.requireNonNull(value, "value");
    this.global = Objects.requireNonNull(global, "global");
    this.workflow = Objects.requireNonNull(workflow, "workflow");
  }
  public TopicLinkBean() {
    super();
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setPath(String path) {
    this.path = path;
  }
  public void setType(String type) {
    this.type = type;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public void setGlobal(Boolean global) {
    this.global = global;
  }
  public void setWorkflow(Boolean workflow) {
    this.workflow = workflow;
  }
  public String getId() {
    return id;
  }
  public String getPath() {
    return path;
  }
  public String getType() {
    return type;
  }
  public String getName() {
    return name;
  }
  public String getValue() {
    return value;
  }
  public Boolean getGlobal() {
    return global;
  }
  public Boolean getWorkflow() {
    return workflow;
  }
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof TopicLinkBean
        && equalTo((TopicLinkBean) another);
  }

  private boolean equalTo(TopicLinkBean another) {
    return id.equals(another.id)
        && type.equals(another.type)
        && path.equals(another.path)
        && name.equals(another.name)
        && value.equals(another.value)
        && global.equals(another.global)
        && workflow.equals(another.workflow);
  }
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + id.hashCode();
    h += (h << 5) + path.hashCode();
    h += (h << 5) + type.hashCode();
    h += (h << 5) + name.hashCode();
    h += (h << 5) + value.hashCode();
    h += (h << 5) + global.hashCode();
    h += (h << 5) + workflow.hashCode();
    return h;
  }
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("TopicLink")
        .omitNullValues()
        .add("id", id)
        .add("path", path)
        .add("type", type)
        .add("name", name)
        .add("value", value)
        .add("global", global)
        .add("workflow", workflow)
        .toString();
  }

  public static TopicLinkBean.Builder builder() {
    return new TopicLinkBean.Builder();
  }
  public static final class Builder {
    private static final long INIT_BIT_ID = 0x1L;
    private static final long INIT_BIT_PATH = 0x2L;
    private static final long INIT_BIT_TYPE = 0x4L;
    private static final long INIT_BIT_NAME = 0x8L;
    private static final long INIT_BIT_VALUE = 0x10L;
    private static final long INIT_BIT_GLOBAL = 0x20L;
    private static final long INIT_BIT_WORKFLOW = 0x40L;
    private long initBits = 0x7fL;

    private @Nullable String id;
    private @Nullable String path;
    private @Nullable String type;
    private @Nullable String name;
    private @Nullable String value;
    private @Nullable Boolean global;
    private @Nullable Boolean workflow;

    private Builder() {
    }
    @CanIgnoreReturnValue 
    public final Builder from(MigrationBuilder.TopicLink instance) {
      Objects.requireNonNull(instance, "instance");
      id(instance.getId());
      path(instance.getPath());
      type(instance.getType());
      name(instance.getName());
      value(instance.getValue());
      global(instance.getGlobal());
      workflow(instance.getWorkflow());
      return this;
    }
    @CanIgnoreReturnValue 
    @JsonProperty("id")
    public final Builder id(String id) {
      this.id = Objects.requireNonNull(id, "id");
      initBits &= ~INIT_BIT_ID;
      return this;
    }
    @CanIgnoreReturnValue 
    @JsonProperty("path")
    public final Builder path(String path) {
      this.path = Objects.requireNonNull(path, "path");
      initBits &= ~INIT_BIT_PATH;
      return this;
    }
    @CanIgnoreReturnValue 
    @JsonProperty("type")
    public final Builder type(String type) {
      this.type = Objects.requireNonNull(type, "type");
      initBits &= ~INIT_BIT_TYPE;
      return this;
    }
    @CanIgnoreReturnValue 
    @JsonProperty("name")
    public final Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      initBits &= ~INIT_BIT_NAME;
      return this;
    }
    @CanIgnoreReturnValue 
    @JsonProperty("value")
    public final Builder value(String value) {
      this.value = Objects.requireNonNull(value, "value");
      initBits &= ~INIT_BIT_VALUE;
      return this;
    }

    @CanIgnoreReturnValue 
    @JsonProperty("global")
    public final Builder global(Boolean global) {
      this.global = Objects.requireNonNull(global, "global");
      initBits &= ~INIT_BIT_GLOBAL;
      return this;
    }
    @CanIgnoreReturnValue 
    @JsonProperty("workflow")
    public final Builder workflow(Boolean workflow) {
      this.workflow = Objects.requireNonNull(workflow, "workflow");
      initBits &= ~INIT_BIT_WORKFLOW;
      return this;
    }
    public TopicLinkBean build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new TopicLinkBean(id, path, type, name, value, global, workflow);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_ID) != 0) attributes.add("id");
      if ((initBits & INIT_BIT_PATH) != 0) attributes.add("path");
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      if ((initBits & INIT_BIT_VALUE) != 0) attributes.add("value");
      if ((initBits & INIT_BIT_GLOBAL) != 0) attributes.add("global");
      if ((initBits & INIT_BIT_WORKFLOW) != 0) attributes.add("workflow");
      return "Cannot build TopicLink, some of required attributes are not set " + attributes;
    }
  }
}
