package io.resys.thena.fs.spi.snapshot;

/*-
 * #%L
 * thena-fs-client
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

import java.util.Optional;

import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class PropsBuilderImpl implements PropsBuilder {
  private final Optional<Props> lock;
  
  private MutableField<JsonObject> propsLabels = new MutableField<JsonObject>();
  private MutableField<JsonObject> propsComments = new MutableField<JsonObject>();
  private MutableField<JsonObject> propsPermissions = new MutableField<JsonObject>();
  private MutableField<JsonObject> propsFlags = new MutableField<JsonObject>();
  private boolean validated = false;

  
  @Override
  public PropsBuilder propsLabels(JsonObject labels) {
    this.propsLabels.withNewValue(labels);
    return this;
  }

  @Override
  public PropsBuilder propsComments(JsonObject comments) {
    this.propsComments.withNewValue(comments);
    return this;
  }

  @Override
  public PropsBuilder propsPermissions(JsonObject permissions) {
    this.propsPermissions.withNewValue(permissions);
    return this;
  }

  @Override
  public PropsBuilder propsFlags(JsonObject flags) {
    this.propsFlags.withNewValue(flags);
    return this;
  }

  @Override
  public void build() {
    RepoAssert.isTrue(
        !(
          propsComments.isNewValueSet() || 
          propsLabels.isNewValueSet() || 
          propsFlags.isNewValueSet() ||
          propsPermissions.isNewValueSet()
        ), 
        () -> "props cannot be all nulls if provided");
    this.validated = true;
  }
  
  public NewPropsResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    final var props = Props.newInstance(
        this.propsLabels.orElse(lock.map(Props::getPropsLabels).orElse(null)),
        this.propsComments.orElse(lock.map(Props::getPropsComments).orElse(null)),
        this.propsPermissions.orElse(lock.map(Props::getPropsPermissions).orElse(null)),
        this.propsFlags.orElse(lock.map(Props::getPropsFlags).orElse(null))
    ).build();
    
    return new NewPropsResult(props);
  }
  

  @Value
  public static class NewPropsResult {
    Props props;
  }
}
