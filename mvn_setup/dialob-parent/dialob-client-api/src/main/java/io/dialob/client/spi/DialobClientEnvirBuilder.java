package io.dialob.client.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import io.dialob.api.form.Form;
import io.dialob.client.api.DialobClient.EnvirBuilder;
import io.dialob.client.api.DialobClient.EnvirCommandFormatBuilder;
import io.dialob.client.api.DialobClient.ProgramEnvir;
import io.dialob.client.spi.support.DialobAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DialobClientEnvirBuilder implements EnvirBuilder {
  private final DialobProgramEnvirFactory factory;
  private ProgramEnvir envir;
  
  @Override
  public EnvirCommandFormatBuilder addCommand() {
    final EnvirBuilder enviBuilder = this;
    return new EnvirCommandFormatBuilder() {
      private String id;
      private String commandJson;
      private boolean cachless;
      private String version;
      
      @Override
      public EnvirCommandFormatBuilder id(String externalId) {
        this.id = externalId;
        return this;
      }
      @Override
      public EnvirCommandFormatBuilder version(String version) {
        this.version = version;
        return this;
      }

      @Override
      public EnvirCommandFormatBuilder form(String commandJson) {
        this.commandJson = commandJson;
        return this;
      }
      @Override
      public EnvirCommandFormatBuilder form(InputStream commandJson) {
        try {
          this.commandJson = new String(commandJson.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
        return this;
      }

      @Override
      public EnvirCommandFormatBuilder cachless() {
        this.cachless = true;
        return this;
      }
      @Override
      public EnvirCommandFormatBuilder form(Form entity) {
        this.id = entity.getId();
        this.version = entity.getRev();
        this.commandJson = JsonObject.mapFrom(entity).encode();
        return this;
      }
      @Override
      public EnvirBuilder build() {
        DialobAssert.notNull(id, () -> "id must be defined!");
        DialobAssert.notNull(version, () -> "version must be defined!");
        DialobAssert.notNull(commandJson, () -> "commandJson must be defined!");
        factory.add(id, version, commandJson, cachless);
        return enviBuilder;
      }


    };
  }
  @Override
  public EnvirBuilder from(ProgramEnvir envir) {
    this.envir = envir;
    return this;
  }
  @Override
  public ProgramEnvir build() {
    return factory.add(envir).build();
  }
}
