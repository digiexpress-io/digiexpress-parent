package io.digiexpress.mig.client.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.mig.client.api.ImmutableSourceForms;
import io.digiexpress.mig.client.api.MigClient.FormFilter;
import io.digiexpress.mig.client.api.SourceForms;
import io.digiexpress.mig.client.api.SourceForms.SourceForm;
import io.digiexpress.mig.client.api.SourceForms.SourceFormDocument;
import io.digiexpress.mig.client.api.SourceForms.SourceFormRev;
import io.digiexpress.mig.client.api.SourceForms.SourceQuestionnaire;
import io.digiexpress.mig.client.spi.loggers.SourceDialobLogger;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SourceDialobQueryFilter {
  private final List<String> onlyRelatedToQuestionnaires;
  private final List<FormFilter> includeFrom;
  private final SourceForms src;
  private final SourceDialobLogger log;
  
  private final List<String> ok_questionnairesFormDocIds = new ArrayList<String>();
  
  private final List<SourceQuestionnaire> ok_questionnaires = new ArrayList<SourceQuestionnaire>();
  private final List<SourceFormDocument> ok_docs = new ArrayList<SourceFormDocument>();
  private final List<SourceFormRev> ok_revs = new ArrayList<SourceFormRev>();
  private final List<SourceForm> ok_forms = new ArrayList<SourceForm>();
  

  public SourceForms apply() {
    visitFormDocIdsFromQuestionnaires();
    visitFormFilters();
    visitForms();
    
    return ImmutableSourceForms.builder()
        .formDocument(ok_docs)
        .formRev(ok_revs)
        .forms(ok_forms)
        .questionnaires(ok_questionnaires)
        .build();
  }
  
  
  private void visitForms() {
    final var forms = src.getForms().stream().collect(Collectors.toMap(e -> (e.getTenant_id() + "/" + e.getName()), e -> e));
    final var docs = ok_docs.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var src_docs = src.getFormDocument().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var src_revs = src.getFormRev().stream().collect(Collectors.toMap(e -> e.getForm_document_id(), e -> e));
    
    for(final var rev : ok_revs) {
      final var form = forms.get(rev.getTenant_id() + "/" + rev.getForm_name());
      if(form == null) {
        log.formNotFound(rev);
        continue;
      }
      
      if(!docs.containsKey(form.getLatest_form_id())) {
        final var unusedFormDoc = src_docs.get(form.getLatest_form_id());
        if(unusedFormDoc == null) {
          log.latestFormDocNotFound(form);
          continue;
        }
        if(!ok_docs.contains(unusedFormDoc)) {
          ok_docs.add(unusedFormDoc);
        }
        
        final var unusedRev = src_revs.get(unusedFormDoc.getId());
        if(unusedRev != null && !ok_revs.contains(unusedRev)) {
          ok_revs.add(unusedRev);
        }
      }
      
      
      if(!ok_forms.contains(form)) {
        ok_forms.add(form);  
      }
      
    }
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
  
  private Tuple2<FormFilter, SourceFormRev> visitSourceDbFormDocumentFilter(SourceFormDocument doc, List<FormFilter> filters) {
    
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
      if(!this.ok_docs.contains(doc)) {
        this.ok_docs.add(doc);
      }
      this.ok_questionnaires.add(questionnaire);
      this.ok_questionnairesFormDocIds.add(questionnaire.getForm_document_id());
    }
  }
  
  
  private boolean isAcceptedQuestionnaire(SourceQuestionnaire questionnaire) {
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
  
  
  private SourceFormRev visitRevision(SourceFormDocument doc, SourceQuestionnaire q) {
    
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
