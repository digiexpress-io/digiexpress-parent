package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewLocaleProps;
import io.resys.limaone.authoring.ImmutableNewLocaleProps.Builder;
import io.resys.limaone.authoring.NewLocale;
import io.resys.limaone.model.ImmutableLocale;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class NewLocaleImpl extends AuthoringTemplate<NewLocaleImpl, Model<Locale>> implements NewLocale {

  private NewLocaleProps props;
  public NewLocaleImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public NewLocale props(NewLocaleProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewLocale props(Consumer<Builder> props) {
    final var builder = ImmutableNewLocaleProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Locale>> build() {
    return config.getPersistence().worldBuilder()
      .docs(BodyType.LOCALE)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body);
      });
  }
  
  private Locale internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var locale = ImmutableLocale.builder()
        .value(props.getLocale())
        .enabled(true);
 
    final var duplicate = world.getLocales().values().stream()
        .filter(p -> p.getBody().getValue().equals(props.getLocale()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      final var msg = "Locale: '" + props.getLocale() + "' already exists!";
      throw new AuthoringException(props, msg);
    }
    
    return locale.build();
  }
}