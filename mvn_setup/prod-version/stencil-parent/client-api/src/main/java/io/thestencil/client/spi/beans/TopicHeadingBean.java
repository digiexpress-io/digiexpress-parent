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

import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.Var;

import io.thestencil.client.api.MigrationBuilder;

public final class TopicHeadingBean implements MigrationBuilder.TopicHeading {
  private String id;
  private String name;
  private Integer order;
  private Integer level;

  public TopicHeadingBean(String id, String name, Integer order, Integer level) {
    this.id = Objects.requireNonNull(id, "id");
    this.name = Objects.requireNonNull(name, "name");
    this.order = Objects.requireNonNull(order, "order");
    this.level = Objects.requireNonNull(level, "level");
  }
  public TopicHeadingBean() {
    super();
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setOrder(Integer order) {
    this.order = order;
  }
  public void setLevel(Integer level) {
    this.level = level;
  }
  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public Integer getOrder() {
    return order;
  }
  public Integer getLevel() {
    return level;
  }
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof TopicHeadingBean
        && equalTo((TopicHeadingBean) another);
  }

  private boolean equalTo(TopicHeadingBean another) {
    return id.equals(another.id)
        && name.equals(another.name)
        && order.equals(another.order)
        && level.equals(another.level);
  }
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + id.hashCode();
    h += (h << 5) + name.hashCode();
    h += (h << 5) + order.hashCode();
    h += (h << 5) + level.hashCode();
    return h;
  }
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("TopicHeading")
        .omitNullValues()
        .add("id", id)
        .add("name", name)
        .add("order", order)
        .add("level", level)
        .toString();
  }

  public static TopicHeadingBean.Builder builder() {
    return new TopicHeadingBean.Builder();
  }
  public static final class Builder {
    private static final long INIT_BIT_ID = 0x1L;
    private static final long INIT_BIT_NAME = 0x2L;
    private static final long INIT_BIT_ORDER = 0x4L;
    private static final long INIT_BIT_LEVEL = 0x8L;
    private long initBits = 0xfL;

    private @Nullable String id;
    private @Nullable String name;
    private @Nullable Integer order;
    private @Nullable Integer level;

    private Builder() {
    }
    public final Builder from(MigrationBuilder.TopicHeading instance) {
      Objects.requireNonNull(instance, "instance");
      id(instance.getId());
      name(instance.getName());
      order(instance.getOrder());
      level(instance.getLevel());
      return this;
    }
    public final Builder id(String id) {
      this.id = Objects.requireNonNull(id, "id");
      initBits &= ~INIT_BIT_ID;
      return this;
    }
    public final Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      initBits &= ~INIT_BIT_NAME;
      return this;
    }
    public final Builder order(Integer order) {
      this.order = Objects.requireNonNull(order, "order");
      initBits &= ~INIT_BIT_ORDER;
      return this;
    }
    public final Builder level(Integer level) {
      this.level = Objects.requireNonNull(level, "level");
      initBits &= ~INIT_BIT_LEVEL;
      return this;
    }
    public TopicHeadingBean build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new TopicHeadingBean(id, name, order, level);
    }
    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_ID) != 0) attributes.add("id");
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      if ((initBits & INIT_BIT_ORDER) != 0) attributes.add("order");
      if ((initBits & INIT_BIT_LEVEL) != 0) attributes.add("level");
      return "Cannot build TopicHeading, some of required attributes are not set " + attributes;
    }
  }
}
