package io.dialob.client.api;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormValidationError;
import io.dialob.api.proto.Actions;
import io.dialob.api.proto.ValueSet;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.ContextValue;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.dialob.client.api.DialobStore.StoreEntity;
import io.dialob.client.api.DialobStore.StoreState;
import io.dialob.program.DialobProgram;
import io.smallrye.mutiny.Uni;

public interface DialobClient {
  
  //origin-dialob-session-boot all tests
  ProgramBuilder program();
  QuestionnaireExecutorBuilder executor(ProgramEnvir envir);   
  EnvirBuilder envir();

  DialobClientConfig getConfig();
  DialobStore store();
  RepoBuilder repo();

  interface QuestionnaireExecutorBuilder {
    QuestionnaireExecutor create(String id, String rev, Consumer<QuestionnaireInit> initWith) throws DocumentNotFoundException ;
    QuestionnaireExecutor create(String id, Consumer<QuestionnaireInit> initWith) throws DocumentNotFoundException ;
    QuestionnaireExecutor restore(Questionnaire queestionnaire) throws DocumentNotFoundException ;
  }
  
  interface QuestionnaireInit {
    QuestionnaireInit activeItem(String activeItem); // TODO:: not needed
    QuestionnaireInit status(Questionnaire.Metadata.Status status); // TODO:: always new, NEW - NEW DOC, user not touched, OPEN - user has started
    QuestionnaireInit valueSets(List<ValueSet> valueSets); // TODO:: not needed
    QuestionnaireInit questionnaire(Questionnaire questionnaire); // TODO:: required why?
    QuestionnaireInit submitUrl(String submitUrl); //TODO:: classifier on submit/ not really needed
    
    QuestionnaireInit id(String id);
    QuestionnaireInit rev(String rev);
    QuestionnaireInit creator(String owner);
    QuestionnaireInit owner(String owner);
    QuestionnaireInit language(String language); //Optional
    QuestionnaireInit additionalProperties(Map<String, Object> additionalProperties); //Optional
    QuestionnaireInit contextValues(List<ContextValue> contextValues); //Optional
    QuestionnaireInit answers(List<Answer> answers); //Optional
  }
  
  interface QuestionnaireExecutor {
    // TODO:: only in tests false, everywhere else true???
    QuestionnaireExecutor createOnly(boolean createOnly);
    QuestionnaireExecutor actions(Actions actions);
    Actions execute();
    ExecutorBody executeAndGetBody();
    QuestionnaireSession toSession();
  }
  
  @Value.Immutable
  interface ExecutorBody {
    Actions getActions();
    Questionnaire getQuestionnaire();
  }
  
  interface ProgramBuilder {
    ProgramBuilder form(FormDocument form);
    DialobProgram build();
  }
  
  interface EnvirBuilder {
    EnvirBuilder from(ProgramEnvir envir);
    EnvirBuilder from(StoreState envir);
    EnvirCommandFormatBuilder addCommand();
    ProgramEnvir build();
  }

  
  interface EnvirCommandFormatBuilder {
    EnvirCommandFormatBuilder id(String externalId);
    EnvirCommandFormatBuilder version(String version);
    EnvirCommandFormatBuilder cachless();

    EnvirCommandFormatBuilder rev(String json);
    EnvirCommandFormatBuilder form(String json);
    EnvirCommandFormatBuilder form(InputStream json);
    
    EnvirCommandFormatBuilder rev(StoreEntity entity);
    EnvirCommandFormatBuilder form(StoreEntity entity);
    EnvirCommandFormatBuilder release(StoreEntity entity);
    
    EnvirCommandFormatBuilder form(Form entity);    
    
    EnvirBuilder build();
  }

  interface RepoBuilder {
    RepoBuilder repoName(String repoName);
    RepoBuilder headName(String headName);
    Uni<DialobClient> create();
    DialobClient build();
  }
  
  
  interface ProgramEnvir {
    ProgramWrapper findByFormId(String formId)  throws DocumentNotFoundException; 
    ProgramWrapper findByFormIdAndRev(String formId, String formRev)  throws DocumentNotFoundException;
    List<ProgramWrapper> findAll();
    
    Map<String, ProgramEnvirValue<?>> getValues();
  }
  
  interface ProgramEnvirValue<T extends DialobDocument> extends Serializable {
    StoreEntity getSource();
    @JsonIgnore
    T getDocument();
  }
  
  @Value.Immutable  
  interface RevisionWrapper extends ProgramEnvirValue<FormRevisionDocument> { }
  
  @Value.Immutable  
  interface ReleaseWrapper extends ProgramEnvirValue<FormReleaseDocument> { }
  
  @Value.Immutable
  interface ProgramWrapper extends ProgramEnvirValue<FormDocument> {
    String getId();
    ProgramStatus getStatus();
    
    List<ProgramMessage> getErrors();
    @JsonIgnore
    Optional<DialobProgram> getProgram();
  } 
  
  @Value.Immutable
  interface ProgramMessage {
    String getId();
    
    @Nullable
    String getMsg();
    
    @Nullable
    FormValidationError getSrc();
    @JsonIgnore
    @Nullable
    Exception getException();
  }
  
  enum ProgramStatus { 
    UP, 
    AST_ERROR, 
    PROGRAM_ERROR, 
    DEPENDENCY_ERROR }

  interface TypesMapper {
    String toString(InputStream entity);                 // just read the data
    Questionnaire readQuestionnaire(InputStream entity); // just read the data
    Form readForm(String entity);                        // just read the data
    FormReleaseDocument readReleaseDoc(String entity);   // just read the data
    FormDocument readFormDoc(String entity);             // just read the data
    FormRevisionDocument readFormRevDoc(String entity);  // just read the data

    String toJson(Object anyObject); // writes object as a json string
    
    
    FormReleaseDocument toFormReleaseDoc(StoreEntity store);
    FormRevisionDocument toFormRevDoc(StoreEntity store);
    FormDocument toFormDoc(StoreEntity store);
    
    String toStoreBody(FormReleaseDocument anyObject);    
    String toStoreBody(FormRevisionDocument anyObject);
    String toStoreBody(FormDocument anyObject);
  }
}
