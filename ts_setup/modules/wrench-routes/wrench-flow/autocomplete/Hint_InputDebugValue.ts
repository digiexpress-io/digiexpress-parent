import { languages } from 'monaco-editor';
import { HdesApi } from '@dxs-ts/wrench-api';

import { Container } from './Hint';
import { HintUtils } from './HintUtils';
import { CompletionItemBuilder } from './CompletionItemBuilder';


export class Hint_InputDebugValue {
  static accept(container: Container): languages.CompletionItem[] {
 
    const node = container.navDesc.node;
    const result: languages.CompletionItem[] = [];

    if (!node || node.type !== 'FLOW_INPUT') {
      return result;
    }

    if (HintUtils.get("debugValue", node.value)) {
      return result;
    }

    if (!HintUtils.isEmptyLine(container)) {
      return result;
    }

    result.push(new CompletionItemBuilder(container.model, container.modelPosition)
      .id("debugValue")
      .addField("debugValue", { indent: 4, value: "\"\"" })
      .build()
    );
    return result;
  }
}