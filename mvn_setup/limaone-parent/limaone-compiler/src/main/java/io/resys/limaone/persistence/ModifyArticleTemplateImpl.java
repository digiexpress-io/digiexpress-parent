package io.resys.limaone.persistence;

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

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyArticleTemplateProps;
import io.resys.limaone.authoring.ImmutableModifyArticleTemplateProps.Builder;
import io.resys.limaone.authoring.ModifyArticleTemplate;
import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.ImmutableArticleTemplate;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyArticleTemplateImpl extends AuthoringTemplate<ModifyArticleTemplateImpl, Model<ArticleTemplate>> implements ModifyArticleTemplate {

  private ModifyArticleTemplateProps props;

  public ModifyArticleTemplateImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyArticleTemplate props(ModifyArticleTemplateProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyArticleTemplate props(Consumer<Builder> props) {
    final var builder = ImmutableModifyArticleTemplateProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<ArticleTemplate>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.ARTICLE_TEMPLATE)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getTemplateId(), body.getName(), body);
      });
  }
  
  private ArticleTemplate internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    final var start = world.getArticleTemplates().get(props.getTemplateId());
    if(start == null) {
      throw new AuthoringException(props, "Article template with id: '" + props.getTemplateId() + "' not found!");
    }
    
    // Check for duplicate name
    final var duplicate = world.getArticleTemplates().values().stream()
        .filter(p -> !p.getId().equals(props.getTemplateId()))
        .filter(p -> p.getBody().getName().equals(props.getName()))
        .findFirst();

    if(duplicate.isPresent()) {
      throw new AuthoringException(props, "Template: '" + props.getName() + "' already exists!");
    }

    return ImmutableArticleTemplate.builder()
      .from(start.getBody())
      .name(props.getName())
      .description(props.getDescription())
      .content(props.getContent())
      .type(props.getType())
      .build();
  }
}
