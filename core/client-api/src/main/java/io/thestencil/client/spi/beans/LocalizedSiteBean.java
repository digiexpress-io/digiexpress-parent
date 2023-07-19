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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.Var;

import io.thestencil.client.api.MigrationBuilder;

public final class LocalizedSiteBean implements MigrationBuilder.LocalizedSite {
  private String id;
  private String images;
  private String locale;
  private Map<String, MigrationBuilder.Topic> topics;
  private Map<String, MigrationBuilder.TopicBlob> blobs;
  private Map<String, MigrationBuilder.TopicLink> links;

  public LocalizedSiteBean(
      String id,
      String images,
      String locale,
      Map<String, ? extends MigrationBuilder.Topic> topics,
      Map<String, ? extends MigrationBuilder.TopicBlob> blobs,
      Map<String, ? extends MigrationBuilder.TopicLink> links) {
    this.id = Objects.requireNonNull(id, "id");
    this.images = Objects.requireNonNull(images, "images");
    this.locale = Objects.requireNonNull(locale, "locale");
    this.topics = new LinkedHashMap<>(topics);
    this.blobs = new LinkedHashMap<>(blobs);
    this.links = new LinkedHashMap<>(links);
  }
  public LocalizedSiteBean() {
    super();
  }
  public void setId(String id) {
    this.id = id;
  }
  public void setImages(String images) {
    this.images = images;
  }
  public void setLocale(String locale) {
    this.locale = locale;
  }
  public void setTopics(Map<String, MigrationBuilder.Topic> topics) {
    this.topics = topics;
  }
  public void setBlobs(Map<String, MigrationBuilder.TopicBlob> blobs) {
    this.blobs = blobs;
  }
  public void setLinks(Map<String, MigrationBuilder.TopicLink> links) {
    this.links = links;
  }
  public String getId() {
    return id;
  }
  public String getImages() {
    return images;
  }
  public String getLocale() {
    return locale;
  }
  public Map<String, MigrationBuilder.Topic> getTopics() {
    return topics;
  }
  public Map<String, MigrationBuilder.TopicBlob> getBlobs() {
    return blobs;
  }
  public Map<String, MigrationBuilder.TopicLink> getLinks() {
    return links;
  }
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof LocalizedSiteBean
        && equalTo((LocalizedSiteBean) another);
  }

  private boolean equalTo(LocalizedSiteBean another) {
    return id.equals(another.id)
        && images.equals(another.images)
        && locale.equals(another.locale)
        && topics.equals(another.topics)
        && blobs.equals(another.blobs)
        && links.equals(another.links);
  }
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + id.hashCode();
    h += (h << 5) + images.hashCode();
    h += (h << 5) + locale.hashCode();
    h += (h << 5) + topics.hashCode();
    h += (h << 5) + blobs.hashCode();
    h += (h << 5) + links.hashCode();
    return h;
  }

  public String toString() {
    return MoreObjects.toStringHelper("LocalizedSite")
        .omitNullValues()
        .add("id", id)
        .add("images", images)
        .add("locale", locale)
        .add("topics", topics)
        .add("blobs", blobs)
        .add("links", links)
        .toString();
  }
  public static LocalizedSiteBean.Builder builder() {
    return new LocalizedSiteBean.Builder();
  }

  
  public static final class Builder {
    private static final long INIT_BIT_ID = 0x1L;
    private static final long INIT_BIT_IMAGES = 0x2L;
    private static final long INIT_BIT_LOCALE = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String id;
    private @Nullable String images;
    private @Nullable String locale;
    private ImmutableMap.Builder<String, MigrationBuilder.Topic> topics = ImmutableMap.builder();
    private ImmutableMap.Builder<String, MigrationBuilder.TopicBlob> blobs = ImmutableMap.builder();
    private ImmutableMap.Builder<String, MigrationBuilder.TopicLink> links = ImmutableMap.builder();

    private Builder() {
    }
 
    public final Builder from(MigrationBuilder.LocalizedSite instance) {
      Objects.requireNonNull(instance, "instance");
      id(instance.getId());
      images(instance.getImages());
      locale(instance.getLocale());
      putAllTopics(instance.getTopics());
      putAllBlobs(instance.getBlobs());
      putAllLinks(instance.getLinks());
      return this;
    }
    public final Builder id(String id) {
      this.id = Objects.requireNonNull(id, "id");
      initBits &= ~INIT_BIT_ID;
      return this;
    }
    public final Builder images(String images) {
      this.images = Objects.requireNonNull(images, "images");
      initBits &= ~INIT_BIT_IMAGES;
      return this;
    }
    public final Builder locale(String locale) {
      this.locale = Objects.requireNonNull(locale, "locale");
      initBits &= ~INIT_BIT_LOCALE;
      return this;
    }
    public final Builder putTopics(String key, MigrationBuilder.Topic value) {
      this.topics.put(key, value);
      return this;
    }
    public final Builder putTopics(Map.Entry<String, ? extends MigrationBuilder.Topic> entry) {
      this.topics.put(entry);
      return this;
    }
    public final Builder topics(Map<String, ? extends MigrationBuilder.Topic> entries) {
      this.topics = ImmutableMap.builder();
      return putAllTopics(entries);
    }
    public final Builder putAllTopics(Map<String, ? extends MigrationBuilder.Topic> entries) {
      this.topics.putAll(entries);
      return this;
    }
    public final Builder putBlobs(String key, MigrationBuilder.TopicBlob value) {
      this.blobs.put(key, value);
      return this;
    }
    public final Builder putBlobs(Map.Entry<String, ? extends MigrationBuilder.TopicBlob> entry) {
      this.blobs.put(entry);
      return this;
    }
    public final Builder blobs(Map<String, ? extends MigrationBuilder.TopicBlob> entries) {
      this.blobs = ImmutableMap.builder();
      return putAllBlobs(entries);
    }
    public final Builder putAllBlobs(Map<String, ? extends MigrationBuilder.TopicBlob> entries) {
      this.blobs.putAll(entries);
      return this;
    }
    public final Builder putLinks(String key, MigrationBuilder.TopicLink value) {
      this.links.put(key, value);
      return this;
    }
    public final Builder putLinks(Map.Entry<String, ? extends MigrationBuilder.TopicLink> entry) {
      this.links.put(entry);
      return this;
    }
    public final Builder links(Map<String, ? extends MigrationBuilder.TopicLink> entries) {
      this.links = ImmutableMap.builder();
      return putAllLinks(entries);
    }
    public final Builder putAllLinks(Map<String, ? extends MigrationBuilder.TopicLink> entries) {
      this.links.putAll(entries);
      return this;
    }
    public LocalizedSiteBean build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new LocalizedSiteBean(id, images, locale, topics.build(), blobs.build(), links.build());
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_ID) != 0) attributes.add("id");
      if ((initBits & INIT_BIT_IMAGES) != 0) attributes.add("images");
      if ((initBits & INIT_BIT_LOCALE) != 0) attributes.add("locale");
      return "Cannot build LocalizedSite, some of required attributes are not set " + attributes;
    }
  }
}
