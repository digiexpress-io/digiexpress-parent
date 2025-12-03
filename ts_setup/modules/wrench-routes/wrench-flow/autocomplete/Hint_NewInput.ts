import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';
import { HintUtils } from './HintUtils';

export class Hint_NewInput {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.src;
    
    const KEY_INPUTS: HdesApi.NodeKeywordTypes = "inputs";
    const KEY_TASKS: HdesApi.NodeKeywordTypes = "tasks";
    
    const inputs = HintUtils.get(KEY_INPUTS, flow);
    if (!inputs) {
      return result;
    }

    let isAround = HintUtils.isInNode(container, inputs, HintUtils.get(KEY_TASKS, flow));
    let isEndOfLine = false;
    const allInputs: HdesApi.AstFlowNode[] = Object.values(inputs.children);
    for (const input of allInputs) {
      if (HintUtils.isEndOfLine(container, input)) {
        isEndOfLine = true;
        break;
      }
      if (HintUtils.isInNode(container, input)) {
        isAround = false;
      }
    }

    if (isAround || isEndOfLine) {
      const builder = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder.id("new input")
        .append(isEndOfLine)
        .addField("{name}", { indent: 2 })
        .addField("required", { indent: 4, value: "true" })
        .addField("type", { indent: 4, value: "STRING" })
        .addField("debugValue", { indent: 4, value: "\"test-string\"" })
        .build());
    }
    
    return result;
  }


}