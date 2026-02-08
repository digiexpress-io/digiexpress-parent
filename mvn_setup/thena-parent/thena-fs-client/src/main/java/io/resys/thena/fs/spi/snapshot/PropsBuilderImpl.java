package io.resys.thena.fs.spi.snapshot;

import java.util.Optional;

import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class PropsBuilderImpl implements PropsBuilder {
  private final Optional<Ref> lock;
  
  private JsonObject propsLabels;
  private JsonObject propsComments;
  private JsonObject propsPermissions;
  private JsonObject propsFlags;
  private boolean validated = false;

  @Override
  public PropsBuilder propsLabels(JsonObject labels) {
    this.propsLabels = labels;
    return this;
  }

  @Override
  public PropsBuilder propsComments(JsonObject comments) {
    this.propsComments = comments;
    return this;
  }

  @Override
  public PropsBuilder propsPermissions(JsonObject permissions) {
    this.propsPermissions = permissions;
    return this;
  }

  @Override
  public PropsBuilder propsFlags(JsonObject flags) {
    this.propsFlags = flags;
    return this;
  }

  @Override
  public void build() {
    validateProps();
    this.validated = true;
  }
  
  public NewPropsResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    final var props = Props.newInstance(
        this.propsLabels,
        this.propsComments, 
        this.propsPermissions,
        this.propsFlags
    ).build();
    
    return new NewPropsResult(props);
  }
  
  private void validateProps() {
    RepoAssert.isTrue(
      !(
        propsComments == null || 
        propsLabels == null || 
        propsFlags == null ||
        propsPermissions == null
      ), 
      () -> "props cannot be all nulls if provided");
  }
  
  @Value
  public static class NewPropsResult {
    Props props;
  }
}
