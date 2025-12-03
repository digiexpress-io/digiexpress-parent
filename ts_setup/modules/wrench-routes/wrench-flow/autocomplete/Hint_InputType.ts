import { languages } from 'monaco-editor';
import { Container, TYPES } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';

export class Hint_InputType {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const inputs = container.flow.src.inputs;
    
    if (!inputs) {
      return result;
    }
    
    const inputsSorted = Object.values(inputs).sort((v1, v2) => v1.start - v2.start);
    for (const input of inputsSorted) {
      if (input.type && container.nav.currentLine !== input.type.start) {
        continue;
      }
      
      for (const type of TYPES) {
        const builder = new CompletionItemBuilder(container.model, container.modelPosition);
        result.push(builder.id("type: " + type).addField("type", { indent: 4, value: type }).build());
      }
    }
    
    return result;
  }
}