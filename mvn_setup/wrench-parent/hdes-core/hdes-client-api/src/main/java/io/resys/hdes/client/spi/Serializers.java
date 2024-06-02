package io.resys.hdes.client.spi;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Arrays;
import java.util.List;

import io.resys.hdes.client.api.ImmutableCacheEntry;
import io.resys.hdes.client.api.ImmutableComposerEntity;
import io.resys.hdes.client.api.ImmutableComposerState;
import io.resys.hdes.client.api.ImmutableCopyAs;
import io.resys.hdes.client.api.ImmutableCreateEntity;
import io.resys.hdes.client.api.ImmutableCreateStoreEntity;
import io.resys.hdes.client.api.ImmutableDebugRequest;
import io.resys.hdes.client.api.ImmutableDebugResponse;
import io.resys.hdes.client.api.ImmutableDeleteAstType;
import io.resys.hdes.client.api.ImmutableImportStoreEntity;
import io.resys.hdes.client.api.ImmutableStoreDump;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ImmutableStoreExceptionMsg;
import io.resys.hdes.client.api.ImmutableStoreState;
import io.resys.hdes.client.api.ImmutableUpdateEntity;
import io.resys.hdes.client.api.ImmutableUpdateStoreEntity;
import io.resys.hdes.client.api.ImmutableUpdateStoreEntityWithBodyType;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.ImmutableAstCommandMessage;
import io.resys.hdes.client.api.ast.ImmutableAstCommandRange;
import io.resys.hdes.client.api.ast.ImmutableAstDecision;
import io.resys.hdes.client.api.ast.ImmutableAstDecisionCell;
import io.resys.hdes.client.api.ast.ImmutableAstDecisionRow;
import io.resys.hdes.client.api.ast.ImmutableAstFlow;
import io.resys.hdes.client.api.ast.ImmutableAstFlowInputType;
import io.resys.hdes.client.api.ast.ImmutableAstService;
import io.resys.hdes.client.api.ast.ImmutableAstServiceRef;
import io.resys.hdes.client.api.ast.ImmutableAstSource;
import io.resys.hdes.client.api.ast.ImmutableCommandsAndChanges;
import io.resys.hdes.client.api.ast.ImmutableFlowAstAutocomplete;
import io.resys.hdes.client.api.ast.ImmutableHeaders;
import io.resys.hdes.client.api.ast.ImmutableTypeDef;
import io.resys.hdes.client.api.programs.ImmutableDecisionLog;
import io.resys.hdes.client.api.programs.ImmutableDecisionLogEntry;
import io.resys.hdes.client.api.programs.ImmutableDecisionProgram;
import io.resys.hdes.client.api.programs.ImmutableDecisionResult;
import io.resys.hdes.client.api.programs.ImmutableDecisionRow;
import io.resys.hdes.client.api.programs.ImmutableDecisionRowAccepts;
import io.resys.hdes.client.api.programs.ImmutableDecisionRowReturns;
import io.resys.hdes.client.api.programs.ImmutableExpressionResult;
import io.resys.hdes.client.api.programs.ImmutableFlowProgram;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStep;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStepBody;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStepConditionalThenPointer;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStepEndPointer;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStepThenPointer;
import io.resys.hdes.client.api.programs.ImmutableFlowProgramStepWhenThenPointer;
import io.resys.hdes.client.api.programs.ImmutableFlowResult;
import io.resys.hdes.client.api.programs.ImmutableFlowResultErrorLog;
import io.resys.hdes.client.api.programs.ImmutableFlowResultLog;
import io.resys.hdes.client.api.programs.ImmutableProgramAssociation;
import io.resys.hdes.client.api.programs.ImmutableProgramContextNamedValue;
import io.resys.hdes.client.api.programs.ImmutableProgramEnvir;
import io.resys.hdes.client.api.programs.ImmutableProgramMessage;
import io.resys.hdes.client.api.programs.ImmutableProgramWrapper;
import io.resys.hdes.client.api.programs.ImmutableServiceProgram;
import io.resys.hdes.client.api.programs.ImmutableServiceResult;
import io.resys.hdes.client.api.programs.ImmutableTagProgram;


public class Serializers {

  public static final List<Class<?>> VALUES = Arrays.asList(
    ImmutableDecisionLog.class,
    ImmutableDecisionLogEntry.class,
    ImmutableDecisionProgram.class,
    ImmutableDecisionResult.class,
    ImmutableDecisionRow.class,
    ImmutableDecisionRowAccepts.class,
    ImmutableDecisionRowReturns.class,
    ImmutableExpressionResult.class,
    ImmutableFlowProgram.class,
    ImmutableFlowProgramStep.class,
    ImmutableFlowProgramStepBody.class,
    ImmutableFlowProgramStepConditionalThenPointer.class,
    ImmutableFlowProgramStepEndPointer.class,
    ImmutableFlowProgramStepThenPointer.class,
    ImmutableFlowProgramStepWhenThenPointer.class,
    ImmutableFlowResult.class,
    ImmutableFlowResultErrorLog.class,
    ImmutableFlowResultLog.class,
    ImmutableProgramAssociation.class,
    ImmutableProgramContextNamedValue.class,
    ImmutableProgramEnvir.class,
    ImmutableProgramMessage.class,
    ImmutableProgramWrapper.class,
    ImmutableServiceProgram.class,
    ImmutableServiceResult.class,
    ImmutableTagProgram.class,
    
    ImmutableAstCommand.class,
    ImmutableAstCommandMessage.class,
    ImmutableAstCommandRange.class,
    ImmutableAstDecision.class,
    ImmutableAstDecisionCell.class,
    ImmutableAstDecisionRow.class,
    ImmutableAstFlow.class,
    ImmutableAstFlowInputType.class,
    ImmutableAstService.class,
    ImmutableAstServiceRef.class,
    ImmutableAstSource.class,
    ImmutableCommandsAndChanges.class,
    ImmutableFlowAstAutocomplete.class,
    ImmutableHeaders.class,
    ImmutableTypeDef.class,
  
    
    ImmutableCacheEntry.class,
    ImmutableComposerEntity.class,
    ImmutableComposerState.class,
    ImmutableCopyAs.class,
    ImmutableCreateEntity.class,
    ImmutableCreateStoreEntity.class,
    ImmutableDebugRequest.class,
    ImmutableDebugResponse.class,
    ImmutableDeleteAstType.class,
    ImmutableImportStoreEntity.class,
    ImmutableStoreDump.class,
    ImmutableStoreEntity.class,
    ImmutableStoreExceptionMsg.class,
    ImmutableStoreState.class,
    ImmutableUpdateEntity.class,
    ImmutableUpdateStoreEntity.class,
    ImmutableUpdateStoreEntityWithBodyType.class
  );
}
