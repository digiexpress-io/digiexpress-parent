import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { Fs } from '@dxs-ts/fs-api';
import { HintUtils } from './HintUtils';

export class Hint_FlowDesc {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.parseTree;
    
    const KEY_DESC: Fs.YamlFlowKeyword = "description";
    const BEFORE: Fs.YamlFlowKeyword[] = ['inputs', 'tasks'];
    
    if (flow.description || !flow.id) {
      return result;
    }

    const before = BEFORE
      .filter(name => HintUtils.isNonNull(name, flow))
      .map(name => HintUtils.get(name, flow));
      
    if (!HintUtils.isBefore(container, before)) {
      return result;
    }
    
    if (!HintUtils.isAfter(container, [flow.id])) {
      return result;
    }

    const builder = new CompletionItemBuilder(container.model, container.modelPosition);
    result.push(builder.id('description').addField(KEY_DESC).build());
    
    return result;
  }
}