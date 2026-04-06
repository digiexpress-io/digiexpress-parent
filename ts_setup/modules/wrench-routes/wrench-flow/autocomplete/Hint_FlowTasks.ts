import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';
import { HintUtils } from './HintUtils';


export class Hint_FlowTasks {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.parseTree;
    
    const KEY_INPUTS: HdesApi.NodeKeywordTypes = "inputs";
    const KEY_TASKS: HdesApi.NodeKeywordTypes = "tasks";
    
    if (HintUtils.get(KEY_TASKS, flow)) {
      return result;
    }
    
    const inputs = HintUtils.get(KEY_INPUTS, flow);
    if (!inputs) {
      return result;
    }
    
    if (!HintUtils.isAfter(container, [inputs])) {
      return result;
    }
    
    const builder = new CompletionItemBuilder(container.model, container.modelPosition);
    result.push(builder.id("tasks block").addField(KEY_TASKS).build());
    
    return result;
  }


}