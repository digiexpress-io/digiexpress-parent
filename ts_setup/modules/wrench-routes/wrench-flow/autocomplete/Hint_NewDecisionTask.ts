import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';

export class Hint_NewDecisionTask {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const node = container.navDesc.node;
    if(!node) {
      return result;
    }

    const isTasks = node.type === 'FLOW_TASKS' || (node.type === 'FLOW_TASK_ASSET_INPUTS' && node.isComplete);
    const isTask = node.type === 'FLOW_TASK' && HintUtils.get('id', node.value) && HintUtils.get('then', node.value);
    
    if (!(isTasks || isTask)) {
      return result;
    }

    // Decision task
    const builder = new CompletionItemBuilder(container.model, container.modelPosition)
      .id("new decision task");
    
    if(isTasks) {
      builder
        .addField("- {name}", { indent: 2 })
        .addField("id", { indent: 6, value: "{id}" })
        .addField("then", { indent: 6, value: "next" });
    }

    builder
      .addField("{serviceType}", { indent: 6 })
      .addField("ref", { indent: 8, value: "{ref}" })
      .addField("collection", { indent: 8, value: "false" })
      .addField("inputs", { indent: 8 })
      .guided("decision-task");
  
    result.push(builder.build());
    
    return result;
  }
}