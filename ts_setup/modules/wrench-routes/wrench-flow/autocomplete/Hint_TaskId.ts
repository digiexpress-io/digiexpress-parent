import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';

export class Hint_TaskId {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    if (!container.navDesc.node) {
      return result;
    }

    // Handle missing id on FLOW_TASK
    if (container.navDesc.node.type === 'FLOW_TASK' && !container.navDesc.node.value.id) {
      const task = container.navDesc.node.value;
      const builder = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder
        .id("id: ")
        .append(false)
        .addField("id", { indent: 6, value: task.keyword })
        .build());
    }

    return result;
  }
}