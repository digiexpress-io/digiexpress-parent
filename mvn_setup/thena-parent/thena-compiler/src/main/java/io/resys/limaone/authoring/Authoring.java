package io.resys.limaone.authoring;

import io.resys.limaone.authoring.article.NewArticle;
import io.resys.limaone.authoring.decisiontable.NewDecisionTable;
import io.resys.limaone.authoring.flow.NewFlow;
import io.resys.limaone.authoring.flowtask.NewFlowTask;
import io.resys.limaone.authoring.link.NewLink;
import io.resys.limaone.authoring.locale.NewLocale;
import io.resys.limaone.authoring.page.NewPage;
import io.resys.limaone.authoring.template.NewTemplate;
import io.resys.limaone.authoring.workflow.NewWorkflow;

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
    ModifyPage modifyPage();
    ModifyLink modifyLink();
    ModifyWorkflow modifyWorkflow();  
    ModifyTemplate modifyTemplate();  
  }
  
  interface NewSource {
    NewFlow newFlow();
    NewFlowTask newFlowTask();
    NewDecisionTable newDecisionTable();
    
    NewArticle newArticle();
    NewLocale newLocale();
    NewPage newPage();
    NewLink newLink();
    NewWorkflow newWorkflow();  
    NewTemplate newTemplate();  
  }
  
  interface DeleteSource {
    
  }
}
