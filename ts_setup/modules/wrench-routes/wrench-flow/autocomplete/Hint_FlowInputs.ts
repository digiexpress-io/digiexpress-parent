import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';

import { HintUtils } from './HintUtils'


export class Hint_FlowInputs {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.src;
    
    const KEY_INPUTS: HdesApi.NodeKeywordTypes = "inputs";
    const AFTER: HdesApi.NodeKeywordTypes[] = ['id', 'description'];
    
    const node = HintUtils.get(KEY_INPUTS, flow);
    if (node) {
      return result;
    }
    const after = AFTER
      .filter(name => HintUtils.isNonNull(name, flow))
      .map(name => HintUtils.get(name, flow));
      
    if (!after.length || !HintUtils.isAfter(container, after)) {
      return result;
    }
    
    const builder = new CompletionItemBuilder(container.model, container.modelPosition);
    result.push(builder.id("inputs block")
      .addField(KEY_INPUTS)
      .addField("myInputParam", { indent: 2 })
      .addField("required", { indent: 4, value: true })
      .addField("type", { indent: 4, value: "STRING" })
      .addField("debugValue", { indent: 4, value: "\"test-string\"" })
      .build());
      
    return result;
  }
}