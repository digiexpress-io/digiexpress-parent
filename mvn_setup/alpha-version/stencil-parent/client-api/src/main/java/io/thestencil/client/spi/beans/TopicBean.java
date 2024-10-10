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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Var;

import io.thestencil.client.api.MigrationBuilder;


public final class TopicBean implements MigrationBuilder.Topic {
  private String id;
  private String name;
  private List<String> links;
  private List<MigrationBuilder.TopicHeading> headings;
  private @Nullable String parent;
  private @Nullable String blob;
  
  public TopicBean(
      String id,
      String name,
      Collection<String> links,
      Collection<? extends MigrationBuilder.TopicHeading> headings,
      @Nullable String parent,
      @Nullable String blob) {
    this.id = Objects.requireNonNull(id, "id");
    this.name = Objects.requireNonNull(name, "name");
    this.links = List.copyOf(links);
    this.headings = List.copyOf(headings);
    this.parent = parent;
    this.blob = blob;
  }
  public TopicBean() {
    super();
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setLinks(List<String> links) {
    this.links = links;
  }
  public void setHeadings(List<MigrationBuilder.TopicHeading> headings) {
    this.headings = headings;
  }
  public void setParent(String parent) {
    this.parent = parent;
  }
  public void setBlob(String blob) {
    this.blob = blob;
  }
  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public List<String> getLinks() {
    return links;
  }
  public List<MigrationBuilder.TopicHeading> getHeadings() {
    return headings;
  }
  public @Nullable String getParent() {
    return parent;
  }
  public @Nullable String getBlob() {
    return blob;
  }
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof TopicBean
        && equalTo((TopicBean) another);
  }

  private boolean equalTo(TopicBean another) {
    return id.equals(another.id)
        && name.equals(another.name)
        && links.equals(another.links)
        && headings.equals(another.headings)
        && Objects.equals(parent, another.parent)
        && Objects.equals(blob, another.blob);
  }
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + id.hashCode();
    h += (h << 5) + name.hashCode();
    h += (h << 5) + links.hashCode();
    h += (h << 5) + headings.hashCode();
    h += (h << 5) + Objects.hashCode(parent);
    h += (h << 5) + Objects.hashCode(blob);
    return h;
  }
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Topic")
        .omitNullValues()
        .add("id", id)
        .add("name", name)
        .add("links", links)
        .add("headings", headings)
        .add("parent", parent)
        .add("blob", blob)
        .toString();
  }

  public static TopicBean.Builder builder() {
    return new TopicBean.Builder();
  }

  public static final class Builder {
    private static final long INIT_BIT_ID = 0x1L;
    private static final long INIT_BIT_NAME = 0x2L;
    private long initBits = 0x3L;

    private @Nullable String id;
    private @Nullable String name;
    private ImmutableList.Builder<String> links = ImmutableList.builder();
    private ImmutableList.Builder<MigrationBuilder.TopicHeading> headings = ImmutableList.builder();
    private @Nullable String parent;
    private @Nullable String blob;

    private Builder() {
    }
    public final Builder from(MigrationBuilder.Topic instance) {
      Objects.requireNonNull(instance, "instance");
      id(instance.getId());
      name(instance.getName());
      addAllLinks(instance.getLinks());
      addAllHeadings(instance.getHeadings());
      @Nullable String parentValue = instance.getParent();
      if (parentValue != null) {
        parent(parentValue);
      }
      @Nullable String blobValue = instance.getBlob();
      if (blobValue != null) {
        blob(blobValue);
      }
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
    public final Builder addLinks(String element) {
      this.links.add(element);
      return this;
    } 
    public final Builder addLinks(String... elements) {
      this.links.add(elements);
      return this;
    }
    public final Builder links(Iterable<String> elements) {
      this.links = ImmutableList.builder();
      return addAllLinks(elements);
    } 
    public final Builder addAllLinks(Iterable<String> elements) {
      this.links.addAll(elements);
      return this;
    } 
    public final Builder addHeadings(MigrationBuilder.TopicHeading element) {
      this.headings.add(element);
      return this;
    } 
    public final Builder addHeadings(MigrationBuilder.TopicHeading... elements) {
      this.headings.add(elements);
      return this;
    }
    public final Builder headings(Iterable<? extends MigrationBuilder.TopicHeading> elements) {
      this.headings = ImmutableList.builder();
      return addAllHeadings(elements);
    } 
    public final Builder addAllHeadings(Iterable<? extends MigrationBuilder.TopicHeading> elements) {
      this.headings.addAll(elements);
      return this;
    }
    public final Builder parent(@Nullable String parent) {
      this.parent = parent;
      return this;
    }
    public final Builder blob(@Nullable String blob) {
      this.blob = blob;
      return this;
    }
    public TopicBean build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new TopicBean(id, name, links.build(), headings.build(), parent, blob);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_ID) != 0) attributes.add("id");
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      return "Cannot build Topic, some of required attributes are not set " + attributes;
    }
  }
}
