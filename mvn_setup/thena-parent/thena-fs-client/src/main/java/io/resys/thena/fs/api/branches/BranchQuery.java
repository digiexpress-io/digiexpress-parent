package io.resys.thena.fs.api.branches;

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.entities.Ref;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Query interface for retrieving branch references with filtering capabilities.
 * Supports searching branches by name patterns and retrieving branch metadata.
 */
public interface BranchQuery {

  /**
   * Applies complex branch name filtering using expression builder patterns.
   * Supports operations like startsWith("feature/"), endsWith("-dev"), contains("hotfix"), etc.
   * 
   * @param nameExpr lambda that configures the name expression builder
   * @return query builder for method chaining
   */
  BranchQuery branchName(Consumer<NameExpressionBuilder> nameExpr);
  
  /**
   * Filters to a specific branch by its unique identifier.
   * Used for retrieving exact branch instances or checking branch existence.
   * 
   * @param branchId the unique branch identifier (same as branch name)
   * @return query builder for method chaining
   */
  BranchQuery branchId(String branchId);
  
  /**
   * Executes query that may return zero or one branch result.
   * Safe when unsure if any branches match the specified criteria.
   * 
   * @return reactive stream containing optional branch result
   */
  Uni<Optional<Ref>> findOne();
  
  /**
   * Executes query returning all matching branches as a reactive stream.
   * Efficient for processing multiple branches or when multiple results are expected.
   * 
   * @return reactive stream of all matching branch references
   */
  Multi<Ref> findAll();
  
  /**
   * Executes query expecting exactly one branch result.
   * Throws exception if zero or multiple branches match the criteria.
   * 
   * @return reactive stream containing the single matching branch
   */
  Uni<Ref> getOne();
}
