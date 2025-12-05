import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';


export class Hint_NewInput {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const node = container.navDesc.node;
    
    if (!node) {
      return result;
    }

    const isInInputsBlock = node.type === 'FLOW_INPUTS';
    const isInInputBlock = node.type === 'FLOW_INPUT' && node.isComplete;


    if (isInInputsBlock || isInInputBlock) {
      const builder = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder.id("new input")
        .addField("{name}", { indent: 2 })
        .addField("required", { indent: 4, value: "true" })
        .addField("type", { indent: 4, value: "STRING" })
        .addField("debugValue", { indent: 4, value: "\"test-string\"" })
        .build());
    }
    
    return result;
  }
}