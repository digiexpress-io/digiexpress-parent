import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';

export class Hint_TaskThen {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    if (!container.navDesc.node) {
      return result;
    }

    // Handle missing then on FLOW_TASK
    if (container.navDesc.node.type === 'FLOW_TASK' && !container.navDesc.node.value.then) {
      const task = container.navDesc.node.value;
      const taskId = task.id?.value;
      const selection = this.findAllTaskThen(container, taskId);
      
      for (const then of selection) {
        const builder = new CompletionItemBuilder(container.model, container.modelPosition);
        result.push(builder
          .id(then.text)
          .append(false)
          .addField("then", { indent: 6, value: then.id })
          .build());
      }
    }

    // Handle editing existing then on FLOW_TASK_THEN
    if (container.navDesc.node.type === 'FLOW_TASK_THEN' && container.navDesc.description === 'ON_ELEMENT') {
      const task = container.navDesc.node.parent?.value!;
      const taskId = task.id?.value;
      const selection = this.findAllTaskThen(container, taskId);

      for (const then of selection) {
        const sufix = container.navDesc.node.value.value === then.id ? " - currently selected" : "";
        const builder = new CompletionItemBuilder(container.model, container.modelPosition);
        result.push(builder
          .id(then.text + sufix)
          .append(false)
          .addField("then", { indent: 6, value: then.id })
          .build());
      }
    }

    return result;
  }

  private static findAllTaskThen(container: Container, taskId: string | undefined): { id: string, text: string }[] {
    return taskId ? [
      { id: "end", text: "then: end" },
      ...Object.entries(container.flow.parseTree.tasks)
        .filter(([, body]) => body.id?.value)
        .filter(([, body]) => (body.id?.value !== taskId))
        .map(([name, body]) => ({
          id: body.id?.value,
          text: `then: ${name}`
        }))
    ] : [{ id: "end", text: "then: end" }];
  }
}