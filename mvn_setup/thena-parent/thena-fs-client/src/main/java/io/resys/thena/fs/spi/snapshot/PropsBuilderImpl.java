package io.resys.thena.fs.spi.snapshot;

import java.util.Optional;

import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class PropsBuilderImpl implements PropsBuilder {
  private final Optional<Ref> lock;

  @Override
  public PropsBuilder propsLabels(JsonObject labels) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PropsBuilder propsComments(JsonObject comments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PropsBuilder propsPermissions(JsonObject permissions) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PropsBuilder propsFlags(JsonObject flags) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void build() {
    // TODO Auto-generated method stub
    
  }
  public NewPropsResult close() {
    
  }
  
  @Value
  public static class NewPropsResult {
    Props props;
  }
}
