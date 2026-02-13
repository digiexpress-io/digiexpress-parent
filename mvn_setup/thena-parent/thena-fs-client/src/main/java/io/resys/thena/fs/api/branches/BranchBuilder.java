package io.resys.thena.fs.api.branches;

import java.util.function.Consumer;

import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.entities.Ref.RefTransitives;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

/**
 * Builder for creating and managing branch references with metadata and configuration.
 * Supports branch creation with descriptive information, access controls, and UI customization.
 */
public interface BranchBuilder {

  /**
   * Applies complex branch name filtering using expression builder patterns.
   * Supports operations like startsWith("feature/"), endsWith("-dev"), contains("hotfix"), etc.
   * Used for conditional branch operations or validation.
   * 
   * @param nameExpr lambda that configures the name expression builder
   * @return builder for method chaining
   */
  BranchBuilder branchName(Consumer<NameExpressionBuilder> nameExpr);
  
  /**
   * Sets a descriptive explanation for the branch purpose.
   * Helps team members understand what this branch is for and its intended lifecycle.
   * 
   * @param branchDescription human-readable description, null for no description
   * @return builder for method chaining
   */
  BranchBuilder branchDescription(@Nullable String branchDescription);
  
  /**
   * Sets extension properties for UI configuration and custom behavior.
   * Can include display settings, workflow configurations, or application-specific metadata.
   * Examples: UI colors, default merge strategies, custom validation rules.
   * 
   * @param branchProps JSON object containing extension properties, null for no custom props
   * @return builder for method chaining
   */
  BranchBuilder branchProps(@Nullable JsonObject branchProps);
  
  /**
   * Sets access control and permission rules for branch operations.
   * Defines who can push, merge, delete, or perform other operations on this branch.
   * Enables fine-grained security management for sensitive branches.
   * 
   * @param branchPermissions JSON object containing permission settings, null for default permissions
   * @return builder for method chaining
   */
  BranchBuilder branchPermissions(@Nullable JsonObject branchPermissions);
  
  /**
   * Sets behavioral flags and feature toggles for branch-specific behavior.
   * Controls branch protection rules, automation settings, or special handling.
   * Examples: require reviews, auto-delete on merge, disable force push.
   * 
   * @param branchFlags JSON object containing behavioral flags, null for default behavior
   * @return builder for method chaining
   */
  BranchBuilder branchFlags(@Nullable JsonObject branchFlags);

  /**
   * Executes the branch creation or update operation.
   * Creates the branch reference with all configured metadata and settings.
   * 
   * @return reactive stream containing the branch operation result
   */
  Uni<BranchResult> build();
  
  /**
   * Callback interface for pre-completion branch processing.
   * Provides access to loaded branch context and allows builder modification.
   */
  @FunctionalInterface
  interface BeforeBranchCompletion {
    /**
     * Applies custom logic before tag completion.
     * Can modify the builder based on the loaded tag context and related entities.
     * 
     * @param loaded the loaded branch context with related entities
     * @param builder the branch builder that can be modified
     */
    void apply(RefTransitives loaded, BranchBuilder builder);
  }
  
}
