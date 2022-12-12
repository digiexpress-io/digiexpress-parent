package io.digiexpress.client.api;

import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.digiexpress.client.api.ComposerEntity.CreateMigration;
import io.digiexpress.client.api.ComposerEntity.CreateProjectRevision;
import io.digiexpress.client.api.ComposerEntity.CreateRelease;
import io.digiexpress.client.api.ComposerEntity.CreateDescriptor;
import io.digiexpress.client.api.ComposerEntity.MigrationState;
import io.digiexpress.client.api.ComposerEntity.DefinitionState;
import io.digiexpress.client.api.ComposerEntity.HeadState;
import io.smallrye.mutiny.Uni;

public interface Composer {
  ComposerBuilder create();
  ComposerQuery query();
  
  interface ComposerQuery {
    Uni<HeadState> head();
    Uni<DefinitionState> definition(String definitionId);
    Uni<HeadState> release(String releaseId);
  }
  
  interface ComposerBuilder {
    Uni<Project> revision(CreateProjectRevision init);
    Uni<ServiceDefinition> serviceDescriptor(CreateDescriptor process);
    Uni<ServiceRelease> release(CreateRelease rel);
    Uni<MigrationState> migrate(CreateMigration mig);
  }
}