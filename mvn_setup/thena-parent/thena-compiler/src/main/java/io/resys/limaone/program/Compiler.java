package io.resys.limaone.program;

import java.time.OffsetDateTime;
import java.util.Optional;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.model.Model.ModelWorld;
import jakarta.annotation.Nullable;

public interface Compiler {  
  AST_Parser getParser();
  BundleBuilder compile(ModelWorld world);
  
  interface BundleBuilder {
    // open for user definition
    BundleBuilder id(String id);
    BundleBuilder name(String name);
    BundleBuilder created(OffsetDateTime created);
    
    BundleBuilder externalId(@Nullable String externalId);
    BundleBuilder startDate(@Nullable OffsetDateTime startDate);
    BundleBuilder endDate(@Nullable OffsetDateTime endDate);
    
    // force to internal cache, makes the bundle by default visible to dependencies
    BundleBuilder cacheKey(@Nullable String cacheKey); 
    
    // end result
    Bundle build();
  }
  
  interface Bundle {
    String getId();
    String getName();
    String getExternalId();
    
    OffsetDateTime getCreated();
    OffsetDateTime getStartDate();
    OffsetDateTime getEndDate();

    BundleQuery<DialobProgram> queryDialob();
    BundleQuery<ArticleProgram> queryArticles();
    BundleQuery<FlowTaskProgram> queryFlowTasks();
    BundleQuery<FlowProgram> queryFlows();
    BundleQuery<DecisionProgram> queryDecisions();
  }
  
  
  interface BundleQuery<T> {
    BundleQuery<T> name(String name);
    BundleQuery<T> externalId(String externalId);
    BundleQuery<T> id(String id);
    
    Optional<T> findOne();
    T getOne();
  }
}