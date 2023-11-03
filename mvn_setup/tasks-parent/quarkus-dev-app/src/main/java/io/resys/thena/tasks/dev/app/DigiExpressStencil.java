package io.resys.thena.tasks.dev.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.rest.StencilRestApi;
import io.thestencil.client.rest.StencilRestApiTemplate;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api/stencil")
public class DigiExpressStencil extends StencilRestApiTemplate implements StencilRestApi {

  public DigiExpressStencil(StencilComposer client, ObjectMapper objectMapper) {
    super(client, objectMapper);
  }
}
