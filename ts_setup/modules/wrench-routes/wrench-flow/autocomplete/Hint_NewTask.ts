import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';
import { HintUtils } from './HintUtils';

export class Hint_NewTask {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const node = container.navDesc.node;
    if(!node) {
      return result;
    }

    const isTasks = node.type === 'FLOW_TASKS';
    const isTask = node.type === 'FLOW_TASK' && HintUtils.get('id', node.value);
    
    if (!(isTasks || isTask)) {
      return result;
    }
  
    // Switch task
    if(isTask && !HintUtils.get('then', node.value) || !isTask) {
      const builder1 = new CompletionItemBuilder(container.model, container.modelPosition)
        .id("new switch task");
        
      if(isTasks) {
        builder1
          .addField("- name", { indent: 2 })
          .addField("id", { indent: 6, value: "task-id" });
      }

      builder1
        .addField("switch", { indent: 6 })
        .addField("- caseName1", { indent: 8 })
        .addField("when", { indent: 12, value: "when-boolean-expression" })
        .addField("then", { indent: 12, value: "next-task-id" })
        .addField("- caseName2", { indent: 8 })
        .addField("when", { indent: 12, value: "when-boolean-expression" })
        .addField("then", { indent: 12, value: "next-task-id" });
        
      result.push(builder1.build());
     }

    // Service task
    const builder2 = new CompletionItemBuilder(container.model, container.modelPosition)
      .id("new service task");

    if(isTasks) {
      builder2
        .addField("- {name}", { indent: 2 })
        .addField("id", { indent: 6, value: "{id}" })
        .addField("then", { indent: 6, value: "next" });
    }

    builder2
      .addField("{serviceType}", { indent: 6 })
      .addField("ref", { indent: 8, value: "{ref}" })
      .addField("collection", { indent: 8, value: "false" })
      .addField("inputs", { indent: 8 })
      .guided("service-task");

    result.push(builder2.build());

    // Decision task
    const builder3 = new CompletionItemBuilder(container.model, container.modelPosition)
      .id("new decision task");
    
    if(isTasks) {
      builder3
        .addField("- {name}", { indent: 2 })
        .addField("id", { indent: 6, value: "{id}" })
        .addField("then", { indent: 6, value: "next" });
    }

    builder3
      .addField("{serviceType}", { indent: 6 })
      .addField("ref", { indent: 8, value: "{ref}" })
      .addField("collection", { indent: 8, value: "false" })
      .addField("inputs", { indent: 8 })
      .guided("decision-task");
  
    result.push(builder3.build());
    
    return result;
  }
}