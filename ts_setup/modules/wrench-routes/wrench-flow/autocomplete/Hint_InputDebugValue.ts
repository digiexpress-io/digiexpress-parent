import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';


export class Hint_InputDebugValue {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const inputs = container.flow.src.inputs;
    
    if (!inputs) {
      return result;
    }
    
    const inputsSorted = Object.values(inputs).sort((v1, v2) => v1.start - v2.start);
    for (const input of inputsSorted) {
      if (input.debugValue) {
        continue;
      }
      
      const builder = new CompletionItemBuilder(container.model, container.modelPosition);
      builder.id("debugValue");
      
      if (HintUtils.isInNode(container, input)) {
        builder.addValue("").append(true);
      }
      
      result.push(builder.addField("debugValue", { indent: 4, value: "\"\"" }).build());
    }
    
    return result;
  }


}