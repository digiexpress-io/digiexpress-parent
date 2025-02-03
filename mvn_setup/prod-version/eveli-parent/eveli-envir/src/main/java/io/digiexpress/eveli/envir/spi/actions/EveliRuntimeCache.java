package io.digiexpress.eveli.envir.spi.actions;

import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;

public interface EveliRuntimeCache {
  Optional<EveliRuntime> get();
  EveliRuntime save(EveliRuntime runtime);
}
