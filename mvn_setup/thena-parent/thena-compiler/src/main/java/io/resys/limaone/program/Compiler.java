package io.resys.limaone.program;

import java.time.OffsetDateTime;
import java.util.Optional;

import io.resys.limaone.model.Model.ModelWorld;

public interface Compiler {  
  
  Bundle compile(ModelWorld world);
  
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