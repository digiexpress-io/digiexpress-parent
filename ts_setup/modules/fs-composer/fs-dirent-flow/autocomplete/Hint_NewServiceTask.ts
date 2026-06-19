import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';

export class Hint_NewServiceTask {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    if(!container.navDesc.node) {
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

    // isTasks - insert a brand-new task (with name/id/then + body)
    // isTask - extend the surrounding task with a body only
    let isTasks = false;
    let isTask = false;

    if (!taskAnc) {
      // empty line directly under tasks: (or between/before tasks)
      // always offer a full new task here - the empty-line guard already excludes structural lines like `tasks:` itself
      isTasks = true;
    } else if (HintUtils.isAfterChildrenOf(container, taskAnc.value)) {
      if (HintUtils.taskHasBody(taskAnc.value)) {
        // past a task that already has a body - new task at the tasks: level
        isTasks = true;
      } else if (HintUtils.get('id', taskAnc.value) && HintUtils.get('then', taskAnc.value)) {
        // existing task has id+then but no body yet - extend with a service body
        isTask = true;
      }
    }

    if (!(isTasks || isTask)) {
      return result;
    }

    // Service task
    const builder = new CompletionItemBuilder(container.model, container.modelPosition)
      .id("new service task");

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
      .guided("service-task");

    result.push(builder.build());
    
    return result;
  }
}