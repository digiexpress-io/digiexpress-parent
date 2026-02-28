package io.resys.limaone.authoring;

import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface Authoring {

  ModifySource modifySource();
  NewSource newSource();
  DeleteSource deleteSource();
  
  
  interface ModifySource {
    ModifyFlow modifyFlow();
    ModifyFlowTask modifyFlowTask();
    ModifyDecisionTable modifyDecisionTable();
    
    ModifyArticle modifyArticle();
    ModifyLocale modifyLocale();
    ModifyArticlePage modifyArticlePage();
    ModifyArticleLink modifyArticleLink();
    ModifyArticleWorkflow modifyArticleWorkflow();  
    ModifyArticleTemplate modifyArticleTemplate();  
  }
  
  interface NewSource {
    NewFlow newFlow();
    NewFlowTask newFlowTask();
    NewDecisionTable newDecisionTable();
    

    NewLocale newLocale();
    NewArticle newArticle();
    NewArticlePage newArticlePage();    
    NewArticleLink newArticleLink();
    NewArticleWorkflow newArticleWorkflow();  
    NewArticleTemplate newArticleTemplate();  
  }
  
  interface DeleteSource {
    Uni<Model<?>> deleteAny(String id);
    DeleteArticleLink deleteArticleLink();
    DeleteArticleWorkflow deleteArticleWorkflow();
  }
}
