import { HdesApi } from '@dxs-ts/wrench-api';
import { Position, languages, editor } from 'monaco-editor';
import { AssetsQuery } from './AssetsQuery';
import { AstNav } from './AstNav';
import { Container } from './Hint';

// Import all hint files
import { Hint_FlowId } from './Hint_FlowId';
import { Hint_FlowDesc } from './Hint_FlowDesc';
import { Hint_FlowInputs } from './Hint_FlowInputs';
import { Hint_FlowTasks } from './Hint_FlowTasks';
import { Hint_NewInput } from './Hint_NewInput';
import { Hint_InputType } from './Hint_InputType';
import { Hint_InputRequired } from './Hint_InputRequired';
import { Hint_InputDebugValue } from './Hint_InputDebugValue';
import { Hint_NewSwitchTask } from './Hint_NewSwitchTask';
import { Hint_NewServiceTask } from './Hint_NewServiceTask';
import { Hint_NewDecisionTask } from './Hint_NewDecisionTask';
import { Hint_TaskThen } from './Hint_TaskThen';
import { Hint_TaskId } from './Hint_TaskId';
import { Hint_TaskAssetRef } from './Hint_TaskAssetRef';
import { Hint_TaskBodyMapping } from './Hint_TaskBodyMapping';
import { Hint_TaskInputMapping } from './Hint_TaskInputMapping';

export class CompletionBuilder {
  private _flow?: HdesApi.AstFlow;
  private _site?: HdesApi.Site;
  private _model?: editor.ITextModel;
  private _modelPosition?: Position;

  withFlow(flow: HdesApi.AstFlow): CompletionBuilder {
    this._flow = flow;
    return this;
  }

  withSite(site: HdesApi.Site): CompletionBuilder {
    this._site = site;
    return this;
  }

  withModel(model: editor.ITextModel): CompletionBuilder {
    this._model = model;
    return this;
  }

  withPosition(modelPosition: Position): CompletionBuilder {
    this._modelPosition = modelPosition;
    return this;
  }

  build(): languages.CompletionItem[] {
    if (!this._flow || !this._site || !this._model || !this._modelPosition) {
      throw new Error('CompletionBuilder: Missing required properties. Use withFlow(), withSite(), withModel(), and withPosition()');
    }

    // Create the container with all computed data
    const assetsQuery = new AssetsQuery(this._site);
    const nav = new AstNav(this._flow, assetsQuery, this._model, this._modelPosition);
    const navDesc = nav.getPositionDescription();

    const container: Container = {
      flow: this._flow,
      site: this._site,
      model: this._model,
      modelPosition: this._modelPosition,
      assetsQuery,
      nav,
      navDesc
    };

    // Collect results from all hints
    const result: languages.CompletionItem[] = [];

    // Flow-level hints
    result.push(...Hint_FlowId.accept(container));
    result.push(...Hint_FlowDesc.accept(container));
    result.push(...Hint_FlowInputs.accept(container));
    result.push(...Hint_FlowTasks.accept(container));

    // Input hints
    result.push(...Hint_InputType.accept(container));
    result.push(...Hint_InputRequired.accept(container));
    result.push(...Hint_InputDebugValue.accept(container));
    result.push(...Hint_NewInput.accept(container));

    // Task hints
    result.push(...Hint_NewSwitchTask.accept(container));
    result.push(...Hint_NewServiceTask.accept(container));
    result.push(...Hint_NewDecisionTask.accept(container));
    result.push(...Hint_TaskThen.accept(container));
    result.push(...Hint_TaskId.accept(container));
    result.push(...Hint_TaskAssetRef.accept(container));
    result.push(...Hint_TaskBodyMapping.accept(container));
    result.push(...Hint_TaskInputMapping.accept(container));

    console.log('desc', navDesc, result);
    return result;
  }
}