package io.thestencil.client.api;

/*-
 * #%L
 * stencil-persistence-api
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

import java.util.Arrays;
import java.util.List;

import io.thestencil.client.spi.beans.LocalizedSiteBean;
import io.thestencil.client.spi.beans.SitesBean;
import io.thestencil.client.spi.beans.TopicBean;
import io.thestencil.client.spi.beans.TopicBlobBean;
import io.thestencil.client.spi.beans.TopicHeadingBean;
import io.thestencil.client.spi.beans.TopicLinkBean;

public class Serializers {

  public static final List<Class<?>> VALUES = Arrays.asList(
    SitesBean.class, 
    LocalizedSiteBean.class,
    TopicBlobBean.class,
    TopicBean.class,
    TopicHeadingBean.class,
    TopicLinkBean.class,
    
    ImmutableSiteState.class,
    ImmutableArticle.class,
    ImmutableArticleMutator.class,
    ImmutableCreateArticle.class,
    ImmutableCreateLink.class,
    ImmutableCreateLocale.class,
    ImmutableCreatePage.class,
    ImmutableCreateRelease.class,
    ImmutableCreateWorkflow.class,
    ImmutableEntity.class,
    ImmutableLocaleLabel.class,
    ImmutableLink.class,
    ImmutableLinkArticlePage.class,
    ImmutableLinkMutator.class,
    ImmutableLocale.class,
    ImmutableLocaleMutator.class,
    ImmutablePage.class,
    ImmutablePageMutator.class,
    ImmutableRelease.class,
    ImmutableWorkflow.class,
    ImmutableWorkflowArticlePage.class,
    ImmutableWorkflowMutator.class,
    ImmutableTemplate.class,
    ImmutableCreateTemplate.class,
    ImmutableTemplateMutator.class,
    
    ImmutableTemplateReleaseItem.class,
    ImmutableLocaleReleaseItem.class,
    ImmutableArticleReleaseItem.class,
    ImmutableLinkReleaseItem.class,
    ImmutableWorkflowReleaseItem.class,
    ImmutablePageReleaseItem.class
  );
}
