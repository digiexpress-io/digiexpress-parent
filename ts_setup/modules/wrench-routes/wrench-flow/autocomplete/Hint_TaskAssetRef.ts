import { languages } from 'monaco-editor';
import { Container } from './Hint';
import { CompletionItemBuilder } from './CompletionItemBuilder';

export class Hint_TaskAssetRef {
  static accept(container: Container): languages.CompletionItem[] {
    const result: languages.CompletionItem[] = [];
    
    if (!container.navDesc.node) {
      return result;
    }

    if (container.navDesc.node.type === 'FLOW_TASK_ASSET_REF' && container.navDesc.description === 'ON_ELEMENT') {
      const task = container.navDesc.node.parent?.value!;
      const service = task["service"];
      const decisionTable = task["decisionTable"];
      const target = decisionTable ? decisionTable : service;
      
      if (!target) {
        return result;
      }
      
      const ref = target.children["ref"];
      if (!ref || !this.isInNode(container, ref)) {
        return result;
      }

      const refs = decisionTable ? Object.values(container.site.decisions) : Object.values(container.site.services);
      for (const asset of refs) {
        const sufix = ref.value === asset.ast?.name ? " - currently selected" : "";
        const builder = new CompletionItemBuilder(container.model, container.modelPosition);
        result.push(builder
          .id("ref: " + asset.ast?.name + sufix)
          .addField("ref", { indent: 8, value: asset.ast?.name })
          .build());
      }
    }

    return result;
  }

  private static isInNode(container: Container, node: { start: number, end: number }): boolean {
    return container.nav.currentLine <= node.end && container.nav.currentLine >= node.start;
  }
}