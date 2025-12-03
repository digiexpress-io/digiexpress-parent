import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';
import { HdesApi } from '@dxs-ts/wrench-api';

export class Hint_FlowId {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    const flow = container.flow.src;
    
    const KEY_ID: HdesApi.NodeKeywordTypes = "id";
    const BEFORE: HdesApi.NodeKeywordTypes[] = ['description', 'inputs', 'tasks'];
    
    const node = flow.id;
    if (node != null) {
      return result;
    }

    const before = BEFORE
      .filter(name => this.hasNonNull(name, flow))
      .map(name => this.get(name, flow));

    if (!this.isBefore(container, before)) {
      return result;
    }
    
    const builder = new CompletionItemBuilder(container.model, container.modelPosition);
    result.push(builder.id("id").addField(KEY_ID).build());
    
    return result;
  }

  private static hasNonNull(name: string, node: any): boolean {
    return this.get(name, node) ? true : false;
  }
  
  private static get(keyword: string, node: any): any {
    const result = node.children ? node.children[keyword] : node[keyword];
    return result;
  }

  private static isBefore(container: Container, nodes: (any | undefined | null)[]): boolean {
    for (const current of nodes) {
      if (!current) {
        continue;
      }
      if (container.nav.currentLine >= current.start) {
        return false;
      }
    }
    return true;
  }
}