package io.resys.limaone.spi.dialob.builders;

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

import io.dialob.api.form.Form;
import io.resys.limaone.spi.dialob.FormDb.CreateForm;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateFormImpl implements CreateForm {
  private final FormDbProps db;
  
  private Form form;
  
  @Override
  public CreateForm props(Form form) {
    this.form = Objects.requireNonNull(form, () -> "form must be defined");
    return this;
  }

  @Override
  public Uni<Form> build() {
    Objects.requireNonNull(form, () -> "form must be defined");
    return db.getFormHttp()
        .httpQuery()
        .uri(uri -> uri.append("forms").build()).method(Form.class)
        .postOneObject(form);
  }

}
