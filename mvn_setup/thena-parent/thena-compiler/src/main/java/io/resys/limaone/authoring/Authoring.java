package io.resys.limaone.authoring;

import io.resys.limaone.authoring.article.ModifyArticle;
import io.resys.limaone.authoring.article.NewArticle;
import io.resys.limaone.authoring.articlelink.NewArticleLink;
import io.resys.limaone.authoring.articlepage.NewArticlePage;
import io.resys.limaone.authoring.articletemplate.NewArticleTemplate;
import io.resys.limaone.authoring.articleworkflow.NewArticleWorkflow;
import io.resys.limaone.authoring.decisiontable.NewDecisionTable;
import io.resys.limaone.authoring.flow.NewFlow;
import io.resys.limaone.authoring.flowtask.NewFlowTask;
import io.resys.limaone.authoring.locale.NewLocale;

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
    
  }
}
