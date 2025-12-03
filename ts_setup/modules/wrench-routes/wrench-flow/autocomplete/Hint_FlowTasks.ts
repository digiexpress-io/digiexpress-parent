import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';

export class Hint_FlowTasks {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.src;
    
    const KEY_INPUTS: HdesApi.NodeKeywordTypes = "inputs";
    const KEY_TASKS: HdesApi.NodeKeywordTypes = "tasks";
    
    if (this.get(KEY_TASKS, flow)) {
      return result;
    }
    
    const inputs = this.get(KEY_INPUTS, flow);
    if (!inputs) {
      return result;
    }
    
    if (!this.isAfter(container, [inputs])) {
      return result;
    }
    
    const builder = new CompletionItemBuilder(container.model, container.modelPosition);
    result.push(builder.id("tasks block").addField(KEY_TASKS).build());
    
    return result;
  }

  private static get(keyword: string, node: any): any {
    const result = node.children ? node.children[keyword] : node[keyword];
    return result;
  }

  private static isAfter(container: Container, nodes: any[]): boolean {
    for (const current of nodes) {
      if (!(container.nav.currentLine > current.end)) {
        return false;
      }
    }
    return true;
  }
}