package io.resys.thena.fs.api.branches;

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.entities.Ref;
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
   * Sets the identifier of who created this branch.
   * Used for accountability, notifications, and ownership tracking.
   * 
   * @param branchAuthor the creator's identifier, null if unknown or system-created
   * @return builder for method chaining
   */
  BranchBuilder branchAuthor(@Nullable String branchAuthor);
  
  /**
   * Executes the branch creation or update operation.
   * Creates the branch reference with all configured metadata and settings.
   * 
   * @return reactive stream containing the branch operation result
   */
  Uni<BranchResult> build();
  
  /**
   * Result envelope containing the outcome of a branch operation.
   * Includes the created/updated branch reference and operation status.
   */
  @Value.Immutable
  interface BranchResult extends ThenaEnvelope {
    /**
     * @return the tenant identifier where the branch operation was performed
     */
    String getTenantId();
    
    /**
     * @return the created/updated branch reference, null if operation failed
     */
    @Nullable Ref getBranch();
    
    /**
     * @return the overall status of the branch operation
     */
    CommitResultStatus getStatus();
    
    /**
     * @return list of diagnostic messages from the branch operation
     */
    List<Message> getMessages();
  }
}
