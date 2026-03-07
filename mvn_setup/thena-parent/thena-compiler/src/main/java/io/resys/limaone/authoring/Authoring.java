package io.resys.limaone.authoring;

import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface Authoring {

  ModifyModel modifyModel();
  NewModel newModel();
  DeleteModel deleteModel();
  
  
  interface ModifyModel {
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
  
  interface NewModel {
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
  
  interface DeleteModel {
    Uni<Model<?>> deleteAny(String id);
    DeleteArticleLink deleteArticleLink();
    DeleteArticleWorkflow deleteArticleWorkflow();
  }
  
  
  interface AuthoringModelProps {
    
  }
}
