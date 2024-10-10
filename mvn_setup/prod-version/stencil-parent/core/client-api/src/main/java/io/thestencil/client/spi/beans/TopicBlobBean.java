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

public final class TopicBlobBean implements MigrationBuilder.TopicBlob {
  private String id;
  private String value;

  public TopicBlobBean(String id, String value) {
    this.id = Objects.requireNonNull(id, "id");
    this.value = Objects.requireNonNull(value, "value");
  }
  public TopicBlobBean() {
    super();
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public String getId() {
    return id;
  }
  public String getValue() {
    return value;
  }
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof TopicBlobBean
        && equalTo((TopicBlobBean) another);
  }

  private boolean equalTo(TopicBlobBean another) {
    return id.equals(another.id)
        && value.equals(another.value);
  }
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + id.hashCode();
    h += (h << 5) + value.hashCode();
    return h;
  }
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("TopicBlob")
        .omitNullValues()
        .add("id", id)
        .add("value", value)
        .toString();
  }
  public static TopicBlobBean.Builder builder() {
    return new TopicBlobBean.Builder();
  }
  public static final class Builder {
    private static final long INIT_BIT_ID = 0x1L;
    private static final long INIT_BIT_VALUE = 0x2L;
    private long initBits = 0x3L;

    private @Nullable String id;
    private @Nullable String value;

    private Builder() {
    }
    public final Builder from(MigrationBuilder.TopicBlob instance) {
      Objects.requireNonNull(instance, "instance");
      id(instance.getId());
      value(instance.getValue());
      return this;
    }
    public final Builder id(String id) {
      this.id = Objects.requireNonNull(id, "id");
      initBits &= ~INIT_BIT_ID;
      return this;
    }
    public final Builder value(String value) {
      this.value = Objects.requireNonNull(value, "value");
      initBits &= ~INIT_BIT_VALUE;
      return this;
    }
    public TopicBlobBean build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new TopicBlobBean(id, value);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_ID) != 0) attributes.add("id");
      if ((initBits & INIT_BIT_VALUE) != 0) attributes.add("value");
      return "Cannot build TopicBlob, some of required attributes are not set " + attributes;
    }
  }
}
