package io.thestencil.client.api;

import java.time.OffsetDateTime;

import io.thestencil.client.api.MigrationBuilder.Sites;

public interface StencilEnvir {
  Sites get(OffsetDateTime now);
}
