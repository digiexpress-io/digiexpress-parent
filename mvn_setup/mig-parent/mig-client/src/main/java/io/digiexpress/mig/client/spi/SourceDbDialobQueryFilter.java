package io.digiexpress.mig.client.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.mig.client.api.ImmutableSourceDbDialob;
import io.digiexpress.mig.client.api.SourceDbClient.FormFilter;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbDialob;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbForm;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbFormDocument;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbFormRev;
import io.digiexpress.mig.client.api.SourceDbClient.SourceDbQuestionnaire;
import io.digiexpress.mig.client.spi.loggers.SourceDbDialobQueryLogger;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SourceDbDialobQueryFilter {
  private final List<String> onlyRelatedToQuestionnaires;
  private final List<FormFilter> includeFrom;
  private final SourceDbDialob src;
  private final SourceDbDialobQueryLogger log;
  
  private final List<String> ok_questionnairesFormDocIds = new ArrayList<String>();
  
  private final List<SourceDbQuestionnaire> ok_questionnaires = new ArrayList<SourceDbQuestionnaire>();
  private final List<SourceDbFormDocument> ok_docs = new ArrayList<SourceDbFormDocument>();
  private final List<SourceDbFormRev> ok_revs = new ArrayList<SourceDbFormRev>();
  private final List<SourceDbForm> ok_forms = new ArrayList<SourceDbForm>();
  

  public SourceDbDialob apply() {
    visitFormDocIdsFromQuestionnaires();
    visitFormFilters();
    visitForms();
    
    return ImmutableSourceDbDialob.builder()
        .formDocument(ok_docs)
        .formRev(ok_revs)
        .forms(ok_forms)
        .questionnaires(ok_questionnaires)
        .build();
  }
  
  
  private void visitForms() {
    
  }
  
  private void visitFormFilters() {
    final List<FormFilter> filters = new ArrayList<>(includeFrom);
    
    
    for(final var doc : src.getFormDocument()) {
      final var match = visitSourceDbFormDocumentFilter(doc, filters);
      if(match != null) {
        
        if(!ok_docs.contains(doc)) {
          ok_docs.add(doc);
        }
        if(!ok_revs.contains(match.getItem2())) {
          ok_revs.add(match.getItem2());  
        }
        
        filters.remove(match.getItem1());
      }
    }
    
    for(final var filter : filters) {
      log.formNotFound(filter);
    }
    
  }
  
  private Tuple2<FormFilter, SourceDbFormRev> visitSourceDbFormDocumentFilter(SourceDbFormDocument doc, List<FormFilter> filters) {
    
    final var revFound = visitRevision(doc, null);
    if(revFound == null) {
      return null;
    }
    
    for(final var filter : filters) {
      if(filter.getFormId().isPresent() && filter.getFormId().get().equals(doc.getId())) {
        return Tuple2.of(filter, revFound);
      }

      if(filter.getFormName().equals(revFound.getForm_name()) && filter.getFormTag().equals(revFound.getName())) {
        return Tuple2.of(filter, revFound);
      }
      
    }
    return null;
  }
  
  
  private void visitFormDocIdsFromQuestionnaires() {
    final var docs = src.getFormDocument().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    
    for(final var questionnaire : src.getQuestionnaires()) {
      
      
      
      if(!isAcceptedQuestionnaire(questionnaire)) {        
        log.questionnaireNotUsed(questionnaire);
        continue;
      }
      
      if(!docs.containsKey(questionnaire.getForm_document_id())) {
        log.questionnaireFormNotFound(questionnaire);
        continue;        
      }
      
      final var doc = docs.get(questionnaire.getForm_document_id());
      final var rev = visitRevision(doc, questionnaire);
      
      if(rev == null) {
        continue;
      }
      
      this.ok_revs.add(rev);
      this.ok_docs.add(doc);
      this.ok_questionnaires.add(questionnaire);
      this.ok_questionnairesFormDocIds.add(questionnaire.getForm_document_id());
    }
  }
  
  
  private boolean isAcceptedQuestionnaire(SourceDbQuestionnaire questionnaire) {
    if(onlyRelatedToQuestionnaires.isEmpty()) {
      return true;
    }
    
    if(onlyRelatedToQuestionnaires.contains(questionnaire.getId())) {
      return true;
    }

    if(onlyRelatedToQuestionnaires.contains(questionnaire.getId().replace("-", ""))) {
      return true;
    }
    
    return false;
  }
  
  
  private SourceDbFormRev visitRevision(SourceDbFormDocument doc, SourceDbQuestionnaire q) {
    
    final var rev = src.getFormRev().stream().filter(f -> f.getForm_document_id().equals(doc.getId())).toList();
    if(rev.isEmpty()) {
      log.formRevNotFound(doc, q);
      return null;
    }
    if(rev.size() != 1) {
      log.formRevMoreThenOneRev(doc, rev, q);
      return null;
    }
    
    return rev.get(0);
  }
  
}
