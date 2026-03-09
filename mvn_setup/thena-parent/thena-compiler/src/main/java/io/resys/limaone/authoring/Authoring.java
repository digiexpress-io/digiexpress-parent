package io.resys.limaone.authoring;

import java.time.OffsetDateTime;

import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

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
    
    NewDeployment newDeployment();
    
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
  
  interface AuthorProps {
    @Nullable String getAuthor(); 
    @Nullable OffsetDateTime getCreatedAt();
  }
  
  
  interface AuthoringModelProps {
 
  }
}
