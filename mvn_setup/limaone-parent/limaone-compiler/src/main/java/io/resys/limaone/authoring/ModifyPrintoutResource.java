package io.resys.limaone.authoring;

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

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.PrintoutResource;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface ModifyPrintoutResource {

  ModifyPrintoutResource props(ModifyPrintoutResourceProps props);
  ModifyPrintoutResource props(Consumer<ImmutableModifyPrintoutResourceProps.Builder> props);

  Uni<Model<PrintoutResource>> build();
  Model<PrintoutResource> buildSync();


  @Value.Immutable @JsonSerialize(as = ImmutableModifyPrintoutResourceProps.class) @JsonDeserialize(as = ImmutableModifyPrintoutResourceProps.class)
  interface ModifyPrintoutResourceProps extends AuthoringModelProps {
    String getResourceId();
    @Nullable String getResourceName();
    @Nullable String getContentType();
    @Nullable String getUploadBody();
    @Nullable List<String> getPrintoutPageIds();
  }
}
