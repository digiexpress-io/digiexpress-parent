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

public final class SitesBean implements MigrationBuilder.Sites {
  private Long created;
  private Map<String, MigrationBuilder.LocalizedSite> sites;
  public SitesBean(
      Long created,
      Map<String, ? extends MigrationBuilder.LocalizedSite> sites) {
    this.created = Objects.requireNonNull(created, "created");
    this.sites = new LinkedHashMap<>(sites);
  }
  public SitesBean() {
    super();
  }
  public void setCreated(Long created) {
    this.created = created;
  }
  public void setSites(Map<String, MigrationBuilder.LocalizedSite> sites) {
    this.sites = sites;
  }
  public Long getCreated() {
    return created;
  }
  public Map<String, MigrationBuilder.LocalizedSite> getSites() {
    return sites;
  }
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof SitesBean
        && equalTo((SitesBean) another);
  }

  private boolean equalTo(SitesBean another) {
    return created.equals(another.created)
        && sites.equals(another.sites);
  }
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + created.hashCode();
    h += (h << 5) + sites.hashCode();
    return h;
  }
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Sites")
        .omitNullValues()
        .add("created", created)
        .add("sites", sites)
        .toString();
  }

  public static SitesBean.Builder builder() {
    return new SitesBean.Builder();
  }
  
  public static final class Builder {
    private static final long INIT_BIT_CREATED = 0x1L;
    private long initBits = 0x1L;

    private @Nullable Long created;
    private ImmutableMap.Builder<String, MigrationBuilder.LocalizedSite> sites = ImmutableMap.builder();

    private Builder() {
    } 
    public final Builder from(MigrationBuilder.Sites instance) {
      Objects.requireNonNull(instance, "instance");
      created(instance.getCreated());
      putAllSites(instance.getSites());
      return this;
    }
    public final Builder created(Long created) {
      this.created = Objects.requireNonNull(created, "created");
      initBits &= ~INIT_BIT_CREATED;
      return this;
    } 
    public final Builder putSites(String key, MigrationBuilder.LocalizedSite value) {
      this.sites.put(key, value);
      return this;
    } 
    public final Builder putSites(Map.Entry<String, ? extends MigrationBuilder.LocalizedSite> entry) {
      this.sites.put(entry);
      return this;
    }
    public final Builder sites(Map<String, ? extends MigrationBuilder.LocalizedSite> entries) {
      this.sites = ImmutableMap.builder();
      return putAllSites(entries);
    } 
    public final Builder putAllSites(Map<String, ? extends MigrationBuilder.LocalizedSite> entries) {
      this.sites.putAll(entries);
      return this;
    }
    public SitesBean build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new SitesBean(created, sites.build());
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_CREATED) != 0) attributes.add("created");
      return "Cannot build Sites, some of required attributes are not set " + attributes;
    }
  }
}
