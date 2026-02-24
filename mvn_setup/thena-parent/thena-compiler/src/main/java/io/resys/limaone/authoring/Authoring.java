package io.resys.limaone.authoring;

import io.resys.limaone.authoring.article.ModifyArticle;
import io.resys.limaone.authoring.article.NewArticle;
import io.resys.limaone.authoring.articlelink.DeleteArticleLink;
import io.resys.limaone.authoring.articlelink.ModifyArticleLink;
import io.resys.limaone.authoring.articlelink.NewArticleLink;
import io.resys.limaone.authoring.articlepage.ModifyArticlePage;
import io.resys.limaone.authoring.articlepage.NewArticlePage;
import io.resys.limaone.authoring.articletemplate.ModifyArticleTemplate;
import io.resys.limaone.authoring.articletemplate.NewArticleTemplate;
import io.resys.limaone.authoring.articleworkflow.DeleteArticleWorkflow;
import io.resys.limaone.authoring.articleworkflow.ModifyArticleWorkflow;
import io.resys.limaone.authoring.articleworkflow.NewArticleWorkflow;
import io.resys.limaone.authoring.decisiontable.ModifyDecisionTable;
import io.resys.limaone.authoring.decisiontable.NewDecisionTable;
import io.resys.limaone.authoring.flow.ModifyFlow;
import io.resys.limaone.authoring.flow.NewFlow;
import io.resys.limaone.authoring.flowtask.ModifyFlowTask;
import io.resys.limaone.authoring.flowtask.NewFlowTask;
import io.resys.limaone.authoring.locale.ModifyLocale;
import io.resys.limaone.authoring.locale.NewLocale;
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
