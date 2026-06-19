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

    if (!HintUtils.isEmptyLine(container)) {
      return result;
    }

    const tasksAnc = HintUtils.findAncestor(container.navDesc.node, ['FLOW_TASKS']);
    if (!tasksAnc) {
      return result;
    }
    const taskAnc = HintUtils.findAncestor(container.navDesc.node, ['FLOW_TASK']);

    let isTasks = false;
    let isTask = false;

    if (!taskAnc) {
      isTasks = true;
    } else if (HintUtils.isAfterChildrenOf(container, taskAnc.value)) {
      if (HintUtils.taskHasBody(taskAnc.value)) {
        isTasks = true;
      } else if (HintUtils.get('id', taskAnc.value)) {
        // switch only requires an `id` on the surrounding task
        isTask = true;
      }
    }

    if (!(isTasks || isTask)) {
      return result;
    }

    // switch task - only show on empty lines at end of tasks list or end of a task block (id present)
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