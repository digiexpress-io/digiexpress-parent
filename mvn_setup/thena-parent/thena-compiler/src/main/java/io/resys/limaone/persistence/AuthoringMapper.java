package io.resys.limaone.persistence;

import java.util.Optional;

import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;

public class AuthoringMapper {
  
  
  
  public static Optional<Model<Locale>> resolveLocale(String idOrValue, ModelWorld state) {
    final var localeRef = idOrValue;
    final var locale = state.getLocales().containsKey(localeRef) ? 
        Optional.of(state.getLocales().get(localeRef)) : 
        state.getLocales().values().stream().filter(l -> l.getBody().getValue().equalsIgnoreCase(localeRef)).findFirst();
     return locale;
  }
  
}
