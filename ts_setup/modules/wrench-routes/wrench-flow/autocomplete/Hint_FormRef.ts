import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HintUtils } from './HintUtils';

export class Hint_FormRef {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];

    if (!container.navDesc.node) {
      return result;
    }

    if (container.navDesc.node.type !== 'FLOW_TASK_FORM_REF' || container.navDesc.description !== 'ON_ELEMENT') {
      return result;
    }

    const task = HintUtils.findAncestor(container.navDesc.node, ['FLOW_TASK'])?.value;
    const form = task ? HintUtils.get('form', task) : undefined;
    const ref = form ? HintUtils.get('ref', form) : undefined;

    if (!task || !form || !ref || !HintUtils.isInNode(container, ref)) {
      return result;
    }

    for (const input of HintUtils.getStringFlowInputs(container.flow)) {
      const sufix = ref.value === input.keyword ? " - currently selected" : "";
      const builder = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder
        .id("ref: " + input.keyword + sufix)
        .addField("ref", { indent: 8, value: input.keyword })
        .build());
    }

    for (const output of HintUtils.getStringTaskOutputRefs(container, task.start)) {
      const value = output.taskId + '.' + output.name;
      const sufix = ref.value === value ? " - currently selected" : "";
      const builder = new CompletionItemBuilder(container.model, container.modelPosition);
      result.push(builder
        .id("ref: " + value + " " + output.type + sufix)
        .addField("ref", { indent: 8, value })
        .build());
    }

    return result;
  }
}
