import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { Fs } from '@dxs-ts/fs-api';
import { HintUtils } from './HintUtils';

export class Hint_FlowId {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.parseTree;
    
    const KEY_ID: Fs.YamlFlowKeyword = "id";
    const BEFORE: Fs.YamlFlowKeyword[] = ['description', 'inputs', 'tasks'];
    
    const node = flow.id;
    if (node != null) {
      return result;
    }

    const before = BEFORE
      .filter(name => HintUtils.isNonNull(name, flow))
      .map(name => HintUtils.get(name, flow));

    if (!HintUtils.isBefore(container, before)) {
      return result;
    }
    
    const builder = new CompletionItemBuilder(container.model, container.modelPosition);
    result.push(builder.id("id").addField(KEY_ID).build());
    
    return result;
  }
}