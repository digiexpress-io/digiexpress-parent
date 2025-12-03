import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';


export class Hint_InputRequired {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const inputs = container.flow.src.inputs;
    
    if (!inputs) {
      return result;
    }
    
    const inputsSorted = Object.values(inputs).sort((v1, v2) => v1.start - v2.start);
    for (const input of inputsSorted) {
      if (input.required && container.nav.currentLine !== input.required.start) {
        continue;
      }
      
      const builder1 = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder1.id("required: true").addField("required", { indent: 4, value: "true" }).build());
      
      const builder2 = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder2.id("required: false").addField("required", { indent: 4, value: "false" }).build());
    }
    
    return result;
  }
}