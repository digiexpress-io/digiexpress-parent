package io.resys.thena.fs.spi.branch;

import java.time.OffsetDateTime;
import java.util.Optional;

import io.resys.thena.fs.api.branches.BranchBuilder;
import io.resys.thena.fs.entities.ImmutableRef;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Ref.RefTransitives;
import io.resys.thena.fs.spi.snapshot.MutableField;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class BranchBuilderImpl implements BranchBuilder {
  private final Optional<Ref> prevRef;
  private final String commitId;
  private final RefTransitives refTransitives;  
  private final OffsetDateTime createdAt;
  private final String refAuthor;
  private final Optional<String> refFrom;
  
  private final MutableField<String> branchName = new MutableField<>();
  private final MutableField<String> branchDescription = new MutableField<>();
  private final MutableField<JsonObject> branchProps = new MutableField<>();
  private final MutableField<JsonObject> branchPermissions = new MutableField<>();
  private final MutableField<JsonObject> branchFlags = new MutableField<>();

  private boolean validated = false;

  @Override
  public BranchBuilder branchDescription(String branchDescription) {
    this.branchDescription.withNewValue(branchDescription);
    return this;
  }
  @Override
  public BranchBuilder branchProps(JsonObject branchProps) {
    this.branchProps.withNewValue(branchProps);
    return this;
  }
  @Override
  public BranchBuilder branchPermissions(JsonObject branchPermissions) {
    this.branchPermissions.withNewValue(branchPermissions);
    return this;
  }
  @Override
  public BranchBuilder branchFlags(JsonObject branchFlags) {
    this.branchFlags.withNewValue(branchFlags);
    return this;
  }
  @Override
  public BranchBuilder branchName(String tagName) {
    this.branchName.withNewValue(tagName);
    return this;
  }
  @Override
  public void build() {
    // For new tags, require at minimum tagName and tagAuthor
    if (prevRef.isEmpty()) {
      RepoAssert.isTrue(branchName.isNewValueSet(), () -> "branchName must be set for new tags");
      RepoAssert.notEmpty(branchName.getNewValue(), () -> "branchName cannot be empty");

    } else {
      // For updates, require at least one change
      RepoAssert.isTrue(
          branchName.isNewValueSet() ||
          branchDescription.isNewValueSet() ||
          branchProps.isNewValueSet() ||
          branchPermissions.isNewValueSet() ||
          branchFlags.isNewValueSet(),
          () -> "cannot have empty branch merge (there are no changes)!");
    }

    this.validated = true;
       
  }
  
  public BranchBuilderResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    // Merge with existing tag or use defaults for new tag
    final var finalName = branchName.orElse(prevRef.map(Ref::getRefName).orElse(null));
    final var finalDescription = branchDescription.orElse(prevRef.flatMap(Ref::getRefDescription).orElse(null));
    final var finalAuthor = prevRef.map(r -> r.getRefName()).orElseGet(() -> refAuthor);
    final var finalFlags = branchFlags.orElse(prevRef.flatMap(Ref::getRefFlags).orElse(null));
    final var finalPermissions = branchPermissions.orElse(prevRef.flatMap(Ref::getRefPermissions).orElse(null));
    final var finalProps = branchProps.orElse(prevRef.flatMap(Ref::getRefProps).orElse(null));


    final var id = this.prevRef.map(Ref::getId).orElseGet(() -> OidUtils.genUUID());
    final var ref = ImmutableRef.builder()
        .id(id)
        .commitId(commitId)
        .refName(finalName)
        
        .refAuthor(finalAuthor)
        .refDescription(Optional.ofNullable(finalDescription))

        .refFlags(Optional.ofNullable(finalFlags))
        .refPermissions(Optional.ofNullable(finalPermissions))
        .refProps(Optional.ofNullable(finalProps))        

        .refAuthor(Optional.ofNullable(refAuthor))
        .refCreatedAt(createdAt)
        .refCreatedFrom(refFrom)
            
        .transitives(refTransitives)
        .build();

    return new BranchBuilderResult(ref);
  }
  @Value
  public static class BranchBuilderResult {
    ImmutableRef ref;
  }
}
