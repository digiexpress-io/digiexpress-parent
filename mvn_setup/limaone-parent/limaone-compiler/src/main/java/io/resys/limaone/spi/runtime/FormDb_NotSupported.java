package io.resys.limaone.spi.runtime;

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

import java.util.Optional;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.http.HttpClient.AnyProxy;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class FormDb_NotSupported implements FormDb {
  private final String tenantName = "forms-not-supported";
  
  @Override
  public String getTenantName() {
    return tenantName;
  }
  @Override
  public FormTenant withTenant() {
    return withTenant(tenantName);
  }
  @Override
  public FormTenant withTenant(String tenantIdOrName) {
    return new FormTenant() {
      @Override 
      public String getTenantId() { 
        return tenantIdOrName; 
      }

      @Override
      public FormQuery formQuery() {
        return new FormQuery() {
          @Override
          public FormQuery formTag(String formName, String formVersion) {
            return this;
          }
          @Override
          public FormQuery formId(String formId) {
            return this;
          }
          @Override
          public Uni<Optional<Form>> findOne() {
            return Uni.createFrom().item(Optional.empty());
          }
          @Override
          public Multi<Form> findAll() {
            return Multi.createFrom().empty();
          }
          @Override
          public AnyProxy proxyAnything() {
            throw new RuntimeException("forms are disabled!");
          }
        };
      }

      @Override
      public FormTagQuery formTagQuery() {
        return new FormTagQuery() {
          
          @Override
          public Uni<FormTag> getOneTag(String formName, String tagName) {
            throw new RuntimeException("forms are disabled!");
          }
          
          @Override
          public Multi<FormTag> findAll() {
            return Multi.createFrom().empty();
          }
          
          @Override
          public Multi<FormTag> findAll(String formName) {
            return Multi.createFrom().empty();
          }

          @Override
          public Multi<FormAndTag> flatAll() {
            return Multi.createFrom().empty();
          }
        };
      }

      @Override
      public FormMetaQuery formMetaQuery() {
        return new FormMetaQuery() {
          @Override
          public Multi<FormMetadata> findAll() {
            return Multi.createFrom().empty();
          }
        };
      }

      @Override
      public CreateForm createForm() {
        throw new RuntimeException("forms are disabled!");
      }

      @Override
      public CreateFormTag createFormTag() {
        throw new RuntimeException("forms are disabled!");
      }

      @Override
      public MergeForm mergeForm() {
        throw new RuntimeException("forms are disabled!");
      }

      @Override
      public MergeFormInstance mergeFormInstance() {
        throw new RuntimeException("forms are disabled!");
      }

      @Override
      public FormInstanceQuery formInstanceQuery() {
        throw new RuntimeException("forms are disabled!");
      }

      @Override
      public CreateFormInstance createFormInstance() {
        throw new RuntimeException("forms are disabled!");
      }

      @Override
      public FormFillBuilder createFormFill() {
        throw new RuntimeException("forms are disabled!");
      }

      @Override
      public FormFillQuery formFillQuery() {
        throw new RuntimeException("forms are disabled!");
      }

      @Override
      public FormInstanceFlatDataQuery formInstanceFlatDataQuery() {
        throw new RuntimeException("forms are disabled!");
      }

      @Override
      public FormFillReview formFillReview() {
        throw new RuntimeException("forms are disabled!");
      }
    };
  }
}
