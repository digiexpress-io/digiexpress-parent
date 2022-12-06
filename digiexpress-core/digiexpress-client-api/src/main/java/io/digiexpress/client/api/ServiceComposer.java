package io.digiexpress.client.api;

import io.digiexpress.client.api.ServiceComposerCommand.CreateMigration;
import io.digiexpress.client.api.ServiceComposerCommand.CreateProcess;
import io.digiexpress.client.api.ServiceComposerCommand.CreateRelease;
import io.digiexpress.client.api.ServiceComposerCommand.CreateServiceRevision;
import io.digiexpress.client.api.ServiceComposerState.MigrationState;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.smallrye.mutiny.Uni;

public interface ServiceComposer {
  CreateBuilder create();
  QueryBuilder query();
  
  interface QueryBuilder {
    Uni<ServiceComposerState> head();
    Uni<ServiceComposerState> release(String releaseId);
  }
  
  interface CreateBuilder {
    Uni<ServiceRevisionDocument> revision(CreateServiceRevision init);
    Uni<ServiceDefinitionDocument> process(CreateProcess process);
    Uni<ServiceReleaseDocument> release(CreateRelease rel);
    Uni<MigrationState> migrate(CreateMigration mig);
  }
}