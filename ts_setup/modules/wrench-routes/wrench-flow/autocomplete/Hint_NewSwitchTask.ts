import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';

export class Hint_NewSwitchTask {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const node = container.navDesc.node;
    if(!node) {
      return result;
    }

    const isTasks = node.type === 'FLOW_TASKS' || (node.type === 'FLOW_TASK_ASSET_INPUTS' && node.isComplete);
    const isTask = node.type === 'FLOW_TASK' && 
      HintUtils.get('id', node.value) && 
      !HintUtils.get('then', node.value);
    
    if (!(isTasks || isTask)) {
      return result;
    }
  
    // Switch task - only show when task doesn't have 'then' or when in tasks block
    const builder = new CompletionItemBuilder(container.model, container.modelPosition)
      .id("new switch task");
      
    if(isTasks) {
      builder
        .addField("- name", { indent: 2 })
        .addField("id", { indent: 6, value: "task-id" });
    }

    builder
      .addField("switch", { indent: 6 })
      .addField("- caseName1", { indent: 8 })
      .addField("when", { indent: 12, value: "when-boolean-expression" })
      .addField("then", { indent: 12, value: "next-task-id" })
      .addField("- caseName2", { indent: 8 })
      .addField("when", { indent: 12, value: "when-boolean-expression" })
      .addField("then", { indent: 12, value: "next-task-id" });
      
    result.push(builder.build());
  
    return result;
  }
}