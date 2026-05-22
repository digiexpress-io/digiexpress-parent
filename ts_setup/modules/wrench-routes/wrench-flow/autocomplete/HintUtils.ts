import { HdesApi } from '@dxs-ts/wrench-api';
import { Container } from './Hint';
import { AstNavNode, AstNavNodeType } from './AstNav';

export class HintUtils {
  static get(keyword: HdesApi.NodeKeywordTypes, node: any): any {
    const result = node.children ? node.children[keyword] : node[keyword];
    return result;
  }

  static isNonNull(name: HdesApi.NodeKeywordTypes, node: any): boolean {
    return HintUtils.get(name, node) ? true : false;
  }

  static isInNode(container: Container, node: { start: number, end: number }, endNode?: { start: number, end: number }): boolean {
    let ending = node.end;
    if (endNode) {
      ending = endNode.start - 1;
    }
    return container.nav.currentLine <= ending && container.nav.currentLine >= node.start;
  }


  // returns the nearest ancestor of `node` whose `type` is in `types`
  // useful when `navDesc.node` resolves to a deep block (e.g. FLOW_TASK_ASSET) but the hint cares about the enclosing FLOW_TASK or FLOW_TASKS context
  static findAncestor(node: AstNavNode | undefined, types: AstNavNodeType[]): AstNavNode | undefined {
    let current: AstNavNode | undefined = node;
    while (current) {
      if (types.includes(current.type)) {
        return current;
      }
      current = current.parent;
    }
    return undefined;
  }

  // true when the editor line under the cursor has no non-whitespace content
  static isEmptyLine(container: Container): boolean {
    return container.model.getLineContent(container.modelPosition.lineNumber).trim().length === 0;
  }

  
  // true when the cursor line is past the end line of every direct child of `node`
  // useful to define "after the node's contents"
  static isAfterChildrenOf(container: Container, node: HdesApi.AstFlowNode): boolean {
    const children = node.children ? Object.values(node.children) : [];
    if (children.length === 0) {
      return container.nav.currentLine > node.start;
    }
    return HintUtils.isAfter(container, children);
  }

  // true when a FLOW_TASK already has a body
  // used to decide whether a "new task" hint should insert the body for the current task or a brand-new task
  static taskHasBody(node: HdesApi.AstFlowNode): boolean {
    return HintUtils.isNonNull('service', node)
      || HintUtils.isNonNull('decisionTable', node)
      || HintUtils.isNonNull('switch', node)
      || HintUtils.isNonNull('userTask', node)
      || HintUtils.isNonNull('form', node)
      || HintUtils.isNonNull('returns', node);
  }

  static getStringFlowInputs(flow: HdesApi.AstFlow): HdesApi.AstFlowInputNode[] {
    return Object.values(flow.parseTree.inputs).filter(input => input.type?.value === 'STRING');
  }

  static isStringOutputType(type: string): boolean {
    const base = type.trim().split('<')[0].trim();
    return base === 'STRING' || base === 'String';
  }

  static getStringTaskOutputRefs(container: Container, beforeStart: number): { taskId: string, name: string, type: string }[] {
    const result: { taskId: string, name: string, type: string }[] = [];

    for (const task of Object.values(container.flow.parseTree.tasks)) {
      if (task.start >= beforeStart) {
        continue;
      }

      const taskId = task.id?.value;
      if (!taskId) {
        continue;
      }

      const form = HintUtils.get('form', task);
      if (form) {
        for (const output of HintUtils.getFormOutputFields(container, task)) {
          if (!HintUtils.isStringOutputType(output.type)) {
            continue;
          }
          result.push({ taskId, name: output.name, type: output.type });
        }
        continue;
      }

      const service: HdesApi.AstFlowNode | undefined = task["service"];
      const decisionTable: HdesApi.AstFlowNode | undefined = task["decisionTable"];
      const target = decisionTable ? decisionTable : service;
      if (!target) {
        continue;
      }

      const ref = target.children["ref"];
      if (!ref?.value) {
        continue;
      }

      const linked = container.assetsQuery.findOne(ref.value);
      const returnDefs = linked?.ast?.headers.returnDefs;
      if (!returnDefs) {
        continue;
      }

      for (const typeDef of returnDefs) {
        if (typeDef.valueType !== 'STRING') {
          continue;
        }
        result.push({ taskId, name: typeDef.name, type: typeDef.valueType });
      }
    }

    return result;
  }

  static getFormReturnsCode(container: Container, returnsNode: HdesApi.AstFlowNode | undefined): string {
    if (!returnsNode) {
      return '';
    }

    const lines: string[] = [];
    const totalLines = container.model.getLineCount();

    for (let lineNumber = returnsNode.start + 1; lineNumber <= totalLines; lineNumber++) {
      const line = container.model.getLineContent(lineNumber);
      if (line.trim().length === 0) {
        continue;
      }

      const indent = line.search(/\S/);
      if (indent <= returnsNode.indent) {
        break;
      }

      lines.push(line.substring(indent));
    }

    return lines.join('\n').trim();
  }

  static extractFormOutputFields(returnsCode: string): { name: string, type: string }[] {
    const result: { name: string, type: string }[] = [];
    const pattern = /^\s*final\s+([\w<>[\],.\s]+?)\s+(\w+)\s*=/gm;
    let match: RegExpExecArray | null;

    while ((match = pattern.exec(returnsCode)) !== null) {
      result.push({
        type: match[1].trim(),
        name: match[2],
      });
    }

    return result;
  }

  static getFormOutputFields(container: Container, task: HdesApi.AstFlowTaskNode): { name: string, type: string }[] {
    const form = HintUtils.get('form', task);
    if (!form) {
      return [];
    }

    const returnsNode = HintUtils.get('returns', form);
    return HintUtils.extractFormOutputFields(HintUtils.getFormReturnsCode(container, returnsNode));
  }

  static isBefore(container: Container, nodes: (any | undefined | null)[]): boolean {
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

  static isAfter(container: Container, nodes: any[]): boolean {
    for (const current of nodes) {
      if (!(container.nav.currentLine > current.end)) {
        return false;
      }
    }
    return true;
  }

  static isEndOfLine(container: Container, node: HdesApi.AstFlowNode): boolean {
    const sameLine = node.end === container.nav.currentLine;
    if (!sameLine) {
      return false;
    }

    const last = Object.values(node.children).filter(v => v.end === node.end).reduce(v => v);
    if (!last) {
      return container.nav.currentColumn >= node.value.length;
    }
    return container.nav.currentColumn >= last.source.value.length;
  }


}