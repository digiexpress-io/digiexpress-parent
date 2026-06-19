import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';

export class Hint_NewFormTask {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const node = container.navDesc.node;
    if (!node) {
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
      } else if (HintUtils.get('id', taskAnc.value) && HintUtils.get('then', taskAnc.value)) {
        isTask = true;
      }
    }

    if (!(isTasks || isTask)) {
      return result;
    }

    const builder = new CompletionItemBuilder(container.model, container.modelPosition)
      .id("new form task");

    if (isTasks) {
      builder
        .addField("- Form task", { indent: 2 })
        .addField("id", { indent: 6, value: "formTask" })
        .addField("then", { indent: 6, value: "next" });
    }

    builder
      .addField("form", { indent: 6 })
      .addField("ref", { indent: 8, value: "{ref}" })
      .addField("returns", { indent: 8, value: "|" })
      .addValue('\r\n          final String email = form.context("Email")');

    result.push(builder.build());

    return result;
  }
}
