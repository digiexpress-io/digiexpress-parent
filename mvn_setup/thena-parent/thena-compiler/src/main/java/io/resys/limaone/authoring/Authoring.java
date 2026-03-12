package io.resys.limaone.authoring;

import java.time.OffsetDateTime;

import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;



public interface Authoring {

  WorldQuery worldQuery();
  
  ModifyModel modifyModel();
  NewModel newModel();
  DeleteModel deleteModel();
  
  
  interface WorldQuery {
    WorldQuery docTypes(BodyType ... types); // narrow down some types... otherwise queries all 
    Uni<ModelWorld> findAll();
    ModelWorld findAllSync();
  }
  
  
  interface ModifyModel {
    ModifyDeployment modifyDeployment();
    
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
    DeleteAny deleteAny();
    DeleteArticleLink deleteArticleLink();
    DeleteArticleWorkflow deleteArticleWorkflow();
  }
  
  interface AuthorProps {
    @Nullable String getAuthor(); 
    @Nullable OffsetDateTime getCreatedAt();
  }
  
  // marker interface
  interface AuthoringModelProps {
 
  }
}
