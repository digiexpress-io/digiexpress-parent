package io.resys.limaone.spi.dialob;

import java.util.Optional;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class FormDb_Empty implements FormDb {
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
            throw new RuntimeException("forms are disabled!");
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

    };
  }


}
