package io.resys.sysconfig.client.spi.executor.builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobClient.ProgramEnvirValue;
import io.dialob.client.api.DialobClient.ProgramWrapper;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.ExecutorClient;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSessionBuilder;
import io.resys.sysconfig.client.api.ImmutableSysConfigSession;
import io.resys.sysconfig.client.api.model.ImmutableFillCreated;
import io.resys.sysconfig.client.api.model.ImmutableProcessCreated;
import io.resys.sysconfig.client.api.model.ImmutableStep;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigInstance;
import io.resys.sysconfig.client.api.model.SysConfigInstance;
import io.resys.sysconfig.client.api.model.SysConfigInstance.FillCreated;
import io.resys.sysconfig.client.api.model.SysConfigInstance.ProcessCreated;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.spi.executor.ExecutorStore;
import io.resys.sysconfig.client.spi.executor.exceptions.ExecutorException;
import io.resys.sysconfig.client.spi.support.ErrorMsg;
import io.resys.sysconfig.client.spi.support.SysConfigAssert;
import io.resys.thena.docdb.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SysConfigInstanceBuilderImpl implements ExecutorClient.SysConfigSessionBuilder {
  
  private final ExecutorStore store;
  private final AssetClient assetClient;
  private final Map<String, Serializable> initProps = new HashMap<>();
  private String workflowName;
  private String locale;
  private Instant targetDate;
  private String ownerId;

  @Override public SysConfigSessionBuilder ownerId(String ownerId) { this.ownerId = ownerId; return this; }
  @Override public SysConfigSessionBuilder workflowName(String workflowName) { this.workflowName = workflowName; return this; }
  @Override public SysConfigSessionBuilder locale(String locale) { this.locale = locale; return this; }
  @Override public SysConfigSessionBuilder targetDate(Instant now) { this.targetDate = now; return this; }
  @Override public SysConfigSessionBuilder addAllProps(Map<String, Serializable> initVariables) { initVariables.putAll(initVariables); return this; }
  @Override public SysConfigSessionBuilder addProp(String variableName, Serializable variableValue) { initProps.put(variableName, variableValue); return this; }
  @Override
  public Uni<SysConfigSession> build() {
    SysConfigAssert.notEmpty(ownerId, () -> "ownerId can't be empty!");
    SysConfigAssert.notEmpty(workflowName, () -> "workflowName can't be empty!");
    SysConfigAssert.notEmpty(locale, () -> "locale can't be empty!");
    SysConfigAssert.notNull(targetDate, () -> "targetDate must be defined!");

    return store.queryReleases().get(targetDate)
        .onItem().transform(this::doInRelease)
        .onItem().transformToUni(instance -> getForm(instance).onItem().transform(form -> doInForm(instance, form)));
  }
  

  private SysConfigSession doInForm(SysConfigInstance instance, DialobClient.ProgramWrapper form) {
    final var dialobClient = this.assetClient.getConfig().getDialob();
    final var questionnaireSessionId = OidUtils.gen();
    final var envir = new DialobProgramEnvirImpl(form);
    final var newExecutor = dialobClient.executor(envir).create(form.getDocument().getData().getId(), (init) -> init.id(questionnaireSessionId).rev("1"));
    final var dialobExecution = newExecutor.executeAndGetBody();

    final var step_2 = ImmutableStep.<FillCreated>builder()
        .id(OidUtils.gen())
        .targetDate(targetDate)
        .body(ImmutableFillCreated.builder().questionnaireSessionId(questionnaireSessionId).build())
        .build();

    final var state = ImmutableSysConfigInstance.builder()
        .from(instance)
        .addSteps(step_2)
        .build();
    
    return ImmutableSysConfigSession.builder()
        .state(state)
        .form(form)
        .session(dialobExecution.getSession())
        .actions(dialobExecution.getActions())
        .build();
  }
  
  private Uni<DialobClient.ProgramWrapper> getForm(SysConfigInstance instance) {
    return store.queryForms().get(instance.getStepProcessCreated().getBody().getFormId())
        .onItem().transform(form -> {
          if(form.isEmpty()) {
            final var processCreated = instance.getStepProcessCreated().getBody();
            throw new ExecutorException(ErrorMsg.builder()
                .withCode("FORM_NOT_FOUND_FROM_RELEASE")
                .withProps(JsonObject.of("workflowName", workflowName, "locale", locale, "release", processCreated.getReleaseId() + "/" + processCreated.getReleaseName()))
                .withMessage("Can't create new instance of a wk, because release for it does not exist!")
                .toString());
          }
          return form.get();
        });
  }
  
  private SysConfigInstance doInRelease(Optional<SysConfigRelease> release) {
    if(release.isEmpty()) {
      throw new ExecutorException(ErrorMsg.builder()
          .withCode("SYS_CONFIG_RELEASE_NOT_FOUND")
          .withProps(JsonObject.of("targetDate", targetDate ,"workflowName", workflowName, "locale", locale))
          .withMessage("Can't create new instance of a wk, because release for it does not exist!")
          .toString());
    }
    
    final var definition = release.get().getServices().stream()
      .filter(service -> service.getServiceName().equals(workflowName))
      .findFirst();
    
    if(definition.isEmpty()) {
      throw new ExecutorException(ErrorMsg.builder()
          .withCode("WK_NOT_FOUND")
          .withProps(JsonObject.of("workflowName", workflowName, "locale", locale, "release", release.get().getId() + "/" + release.get().getName()))
          .withMessage("Can't create new instance of a wk, because it does not exist!")
          .toString());
    }
    if(!definition.get().getLocales().contains(this.locale)) {
      throw new ExecutorException(ErrorMsg.builder()
          .withCode("WK_LOCALE_NOT_FOUND")
          .withProps(JsonObject.of("workflowName", workflowName, "locale", locale, "release", release.get().getId() + "/" + release.get().getName()))
          .withMessage("Can't create new instance of a wk, because user given locale does not exist!")
          .toString());
    }
    
    final var step_1 = ImmutableStep.<ProcessCreated>builder()
      .id(OidUtils.gen())
      .targetDate(targetDate)
      .body(ImmutableProcessCreated.builder()
          .id(OidUtils.gen())
          .serviceId(definition.get().getId())
          .releaseId(release.get().getId())
          .releaseName(release.get().getName())
          .serviceName(definition.get().getServiceName())
          .flowName(definition.get().getFlowName())
          .formId(definition.get().getFormId())
          .params(initProps)
          .build())
      .build();

    final var state = ImmutableSysConfigInstance.builder()
        .id(OidUtils.gen())
        .ownerId(ownerId)
        .version("1")
        .addSteps(step_1)
        .build();
    
    return state;
  }

  
  
  @RequiredArgsConstructor
  private static class DialobProgramEnvirImpl implements DialobClient.ProgramEnvir {
    private final ProgramWrapper wrapper;
    @Override
    public ProgramWrapper findByFormId(String formId) throws DocumentNotFoundException {
      final var form = wrapper.getDocument().getData();
      SysConfigAssert.isTrue(form.getId().equals(formId), 
          () -> "Fixed envir not set up correctly, expecting formId = '" + form.getId() + "' but was '" + formId + "'!");
      return wrapper;
    }
    @Override
    public ProgramWrapper findByFormIdAndRev(String formId, String formRev) throws DocumentNotFoundException {
      final var form = wrapper.getDocument().getData();
      SysConfigAssert.isTrue(form.getId().equals(formId), () -> "Fixed envir not set up correctly, expecting formId = '" + form.getId() + "' but was '" + formId + "'!");
      SysConfigAssert.isTrue(form.getRev().equals(formRev), () -> "Fixed envir not set up correctly, expecting formRev = '" + form.getRev() + "' but was '" + formRev + "'!");
      return wrapper;
    }
    @Override
    public List<ProgramWrapper> findAll() {
      return Arrays.asList(wrapper);
    }
    @Override
    public Map<String, ProgramEnvirValue<?>> getValues() {
      return Map.of(wrapper.getId(), wrapper);
    }
  }
}
