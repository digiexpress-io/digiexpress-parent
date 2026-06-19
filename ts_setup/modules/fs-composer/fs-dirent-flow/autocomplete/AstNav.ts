import { Fs } from '@dxs-ts/fs-api';
import { Position, editor } from 'monaco-editor';
import { AssetsQuery } from './AssetsQuery';


export type AstNavNodeType = (
  'ROOT' |

  'FLOW_ID' | 
  'FLOW_DESCRIPTION' | 

  'FLOW_INPUTS' | 
  'FLOW_INPUT' | 
  'FLOW_INPUT_ELEMENT' | 

  "FLOW_TASKS" | 

  "FLOW_TASK" | 
  "FLOW_TASK_ID" | 
  "FLOW_TASK_THEN" | 

  'FLOW_TASK_SWITCH' | 
  'FLOW_TASK_SWITCH_CASE' | 
  'FLOW_TASK_SWITCH_WHEN' | 
  'FLOW_TASK_SWITCH_THEN' | 

  'FLOW_TASK_ASSET' | 
  'FLOW_TASK_ASSET_REF' | 
  'FLOW_TASK_ASSET_COLLECTION' | 
  
  'FLOW_TASK_ASSET_INPUTS' | 
  'FLOW_TASK_ASSET_INPUT' |

  'FLOW_TASK_FORM' |
  'FLOW_TASK_FORM_REF' |
  'FLOW_TASK_FORM_RETURNS'
)

export interface AstNavNodeDesc {
  description: 'EMPTY_FILE' | 'ON_ELEMENT' | 'MIDDLE_OF_BLOCK' | 'END_OF_BLOCK', 
  node: AstNavNode | undefined
}


export class AstNav {
  private _flow: Fs.FlowAst;
  private _nodes_raw: Fs.Yaml[];
  private _nodes_nav: AstNavNode[];

  private _model: editor.ITextModel;
  private _modelPosition: Position;

  private _currentColumn: number;  
  private _currentLine: number;  
  private _currentLineContent: string;
  private _currentLineEOL: boolean;

  constructor(
    flow: Fs.FlowAst, 
    query: AssetsQuery,
    model: editor.ITextModel, 
    modelPosition: Position) {

    this._flow = flow;
    this._model = model;

    

    this._currentColumn = modelPosition.column - 1;
    // 1-based - matches backend's CST YAML parser's lineNumber, which is 1-based
    this._currentLine = modelPosition.lineNumber;
    this._modelPosition = modelPosition;
    this._currentLineContent = model.getLineContent(modelPosition.lineNumber);
    this._currentLineEOL = this._currentLineContent.length == this._currentColumn;

    this._nodes_raw = _toNodesRaw(flow.parseTree).sort((a, b) => a.start - b.start);
    this._nodes_nav = _toNodesNav(this._nodes_raw, query);
  }

  get currentLine() { return this._currentLine; }
  get currentColumn() { return this._currentColumn; }
  get modelPosition() { return this._modelPosition; }
  get model() { return this._model; }

  getTree(): AstNavNode | undefined {
    if(this._nodes_nav.length === 0) {
      return undefined;
    }
    return this._nodes_nav[0].getAt(this._currentLine);
  }

  getPositionDescription(): AstNavNodeDesc {
    const node = this.getTree();
    if(!node) {
      return { description: 'EMPTY_FILE', node };
    }
    
    if(node.isElementType) {
      // directly on the element
      const isOnElement = node.value.start === node.value.end && node.value.end === this._currentLine;
      if(isOnElement) {
        return { description: 'ON_ELEMENT', node };
      } 
      
      const parentNode = node.parent?.firstIncomplete;
      if(parentNode) {
        return { description: 'MIDDLE_OF_BLOCK', node: parentNode };  
      }
      return { description: 'ON_ELEMENT', node };
    }
    const endRegion = node.endRegion;
    if(endRegion.start <= this._currentLine && (endRegion.end === undefined || endRegion.end >= this._currentLine)) {
      return { description: 'END_OF_BLOCK', node };
    }

    return { description:  'MIDDLE_OF_BLOCK', node };
  }
}


export class AstNavNode {
  private _parent: AstNavNode | undefined;
  private _previous: AstNavNode | undefined;
  private _next: AstNavNode | undefined;
  private _value: Fs.Yaml;
  private _query: AssetsQuery;
  private _type: AstNavNodeType;

  constructor(value: Fs.Yaml, type: AstNavNodeType, query: AssetsQuery, props: { 
    previous: AstNavNode | undefined;
    next: AstNavNode | undefined;
  }) {

    this._value = value;
    this._previous = props.previous;
    this._next = props.next;
    this._query = query;
    this._type = type;
  }

  withNext(next: AstNavNode) {
    this._next = next;
  }
  withParent(parent: AstNavNode) {
    this._parent = parent;
  }
  getAt(line: number): AstNavNode {
    let base: AstNavNode | undefined = this.base;
    while(base) {
      const start = base.start;
      const end = base.isElementType ? base.end : base.endBlock;
      if(start <= line && end >= line) {
        return base;
      }

      base = base._previous;
    }
    return this.tip;
  }

  get base(): AstNavNode {
    let base: AstNavNode = this;
    while(base._next) {
      base = base._next;
    }
    return base;
  }

  get tip(): AstNavNode {
    let tip: AstNavNode = this;
    while(tip._previous) {
      tip = tip._previous;
    }
    return tip;
  }

  get id(): string {
    return _toId(this._value);
  }

  get end(): number {
    return this._value.end;
  }

  get endBlock(): number {
    if(this.isElementType) {
      return this.end;
    }

    let target: AstNavNode | undefined = this;
    while(target._next) {
      if(target._next.indent < this.indent) {
        return target._next.start - 1;
      }
      target = target._next;
    }

    return this.tip.end;
  }

  get isElementType() {
    const blockTypes: AstNavNodeType[] = [
      'ROOT',
      'FLOW_INPUTS',
      'FLOW_INPUT',
      'FLOW_TASKS',
      'FLOW_TASK',
      'FLOW_TASK_SWITCH',
      'FLOW_TASK_SWITCH_CASE',
      'FLOW_TASK_ASSET',
      'FLOW_TASK_ASSET_INPUTS',
      'FLOW_TASK_FORM'
    ];
    return !blockTypes.includes(this.type);
  }

  get isComplete(): boolean {

    const isValueEmpty = (
        this._value.value === null || 
        this._value.value === undefined || 
        this._value.value === '' || 
        this._value.value === 'null');

    switch(this._value.keyword as Fs.YamlFlowKeyword) {
      case 'tasks':
      case 'switch': return false;

      case 'inputs': return _isNestedInputsComplete(this, this._query);
      case 'decisionTable': return _isDtComplete(this);
      case 'service': return _isServiceComplete(this);
      case 'form': return _isFormComplete(this);
      case 'returns':
        if (this._value.parent?.keyword === 'form') {
          return _isFormReturnsComplete(this);
        }
        return _isReturnsComplete(this);

      case 'debugValue':
      case 'collection':
      case 'ref':
      case 'id': 
      case 'description':
      case 'then':
      case 'when':
      case 'required':
      case 'type': return !isValueEmpty;
    }

    const parent_level_1_keyword: Fs.YamlFlowKeyword | undefined = this._value.parent?.keyword as any;
    switch(parent_level_1_keyword) {
      case 'switch': return _isSwitchCaseComplete(this);
      case 'inputs': return _isInputsComplete(this);
    }

    const parent_level_2_keyword: Fs.YamlFlowKeyword | undefined = this._value.parent?.parent?.keyword as any;
    switch(parent_level_2_keyword) {
      
    }
    return true;
  }

  get firstIncomplete(): AstNavNode | undefined {
    let target: AstNavNode | undefined = this; 
    while(target) {

      if(!target.isComplete) {
        return target;
      }

      if(!target.value.parent) {
        return undefined;
      }
      const next: AstNavNode | undefined  = target.parent;
      if(!next) {
        return undefined;
      }
      if(target?.start === next.start) {
        return next;
      }
      target = next;
    }
    return undefined;
  }

  get parent(): AstNavNode | undefined {
    return this._parent;
  }

  get endRegion() {
    const [lastNode] = Object.values(this._value.children).sort((a, b) => b.start - a.start);
    const start = (lastNode ? lastNode.end : this._value.start)+1;
    const end = this.end;
    return { start, end };
  }
  get indent(): number { return this._value.indent; }
  get start(): number { return this._value.start; }
  get previous() { return this._previous; }
  get next() { return this._next; }
  get value() { return this._value; }
  get type() { return this._type; }
}

function _toNodesNav(nodes: Fs.Yaml[], query: AssetsQuery): AstNavNode[] {
  const collector: AstNavNode[] = [];
  const byId: Record<string, AstNavNode> = {};
  let previous: AstNavNode | undefined = undefined;
  
  for(const node of nodes) {
    const type = _classifyNode(node);
    const current: AstNavNode = new AstNavNode(node, type, query, { previous, next: undefined });
    byId[current.id] = current;

    if(previous) {
      previous.withNext(current);
    }

    collector.push(current);
    previous = current; 
  }

  for(const node of nodes) {
    if(!node.parent) {
      continue;
    }
    const parent: AstNavNode = byId[_toId(node.parent)];
    const current: AstNavNode = byId[_toId(node)];
    current.withParent(parent);
  }

  return collector;
}

function _toId(node: Fs.Yaml): string {
  return node.indent + "/" + node.start + "/" + node.keyword;
}

function _toNodesRaw(
  node: Fs.Yaml, 
  collector?: Fs.Yaml[], 
  parent?: Fs.Yaml
): Fs.Yaml[] {

  if(!collector) {
    collector = [];
  }
  
  const clone = { ...node, parent };
  collector.push(clone);
  for(const child of Object.values(node.children)) {
    _toNodesRaw(child, collector, clone);
  }
  return collector;
}

function _isDtComplete(node: AstNavNode) {
  const keywords: string[] = ['ref', 'collection', 'inputs'];
  for(const keyword of keywords) {
    const item = node.value.children[keyword];
    if(!item) {
      return false;
    }
  }
  return true;
}
function _isServiceComplete(node: AstNavNode) {
  const keywords: string[] = ['ref', 'collection', 'inputs'];
  for(const keyword of keywords) {
    const item = node.value.children[keyword];
    if(!item) {
      return false;
    }
  }
  return true;
}
function _isFormComplete(node: AstNavNode) {
  const keywords: string[] = ['ref', 'returns'];
  for(const keyword of keywords) {
    const item = node.value.children[keyword];
    if(!item) {
      return false;
    }
  }
  return true;
}
function _isFormReturnsComplete(node: AstNavNode) {
  return node.end > node.start;
}
function _isReturnsComplete(node: AstNavNode) {
  const keywords: string[] = ['collection', 'inputs'];
  for(const keyword of keywords) {
    const item = node.value.children[keyword];
    if(!item) {
      return false;
    }
  }
  return true;
}
function _isSwitchCaseComplete(node: AstNavNode) {
  const keywords: string[] = ['when', 'then'];
  for(const keyword of keywords) {
    const item = node.value.children[keyword];
    if(!item) {
      return false;
    }
  }
  return true;
}

function _isInputsComplete(node: AstNavNode) {
  const keywords: string[] = ['required', 'type', 'debugValue'];
  for(const keyword of keywords) {
    const item = node.value.children[keyword];
    if(!item) {
      return false;
    }
  }
  return true;
}

function _isNestedInputsComplete(node: AstNavNode, query: AssetsQuery) {
  const ref = node.value.parent?.ref

  if(!ref) {
    return false;
  }

  const linkedAsset = query.findOne(ref.value ?? '');
  if(!linkedAsset) {
    return false;
  }

  const expectedInputs = linkedAsset.ast?.headers?.acceptDefs;
  if(!expectedInputs) {
    return false;
  }

  for(const expectedInput of expectedInputs) {
    const isUndefined = !node.value.children[expectedInput.name];
    if(isUndefined) {
      return false;
    }
  }
  return true;
}


function _classifyNode(node: Fs.Yaml): AstNavNodeType {
  const keyword: Fs.YamlFlowKeyword | undefined = node.keyword as any;
  const parent_level_1_keyword: Fs.YamlFlowKeyword | undefined = node.parent?.keyword as any;
  const parent_level_2_keyword: Fs.YamlFlowKeyword | undefined = node.parent?.parent?.keyword as any;
  const parent_level_3_keyword: Fs.YamlFlowKeyword | undefined = node.parent?.parent?.parent?.keyword as any;
  const parent_level_4_keyword: Fs.YamlFlowKeyword | undefined = node.parent?.parent?.parent?.parent?.keyword as any;

  const parentPath = [
    parent_level_4_keyword,
    parent_level_3_keyword,
    parent_level_2_keyword,
    parent_level_1_keyword,
    keyword
  ].filter(k => k !== undefined).join('.');
  // Root level
  if(keyword === 'id' && node.indent === 0) {
    return 'FLOW_ID';
  }
  if(keyword === 'description' && node.indent === 0) {
    return 'FLOW_DESCRIPTION';
  }
  
  // Inputs section
  if(keyword === 'inputs' && node.indent === 0) {
    return 'FLOW_INPUTS';
  }
  if(parent_level_1_keyword === 'inputs' && node.parent?.indent === 0) {
    return 'FLOW_INPUT';
  }
  if(parent_level_2_keyword === 'inputs' && node.parent?.parent?.indent === 0) {
    return 'FLOW_INPUT_ELEMENT';
  }
  
  // Tasks section
  if(keyword === 'tasks' && node.indent === 0) {
    return 'FLOW_TASKS';
  }

  if(parent_level_1_keyword === 'tasks' && node.parent?.indent === 0) {
    return 'FLOW_TASK';
  }
  
  // Task properties
  if(keyword === 'id' && parent_level_2_keyword === 'tasks') {
    return 'FLOW_TASK_ID';
  }
  if(keyword === 'then' && parent_level_2_keyword === 'tasks') {
    return 'FLOW_TASK_THEN';
  }
  
  // Switch
  if(keyword === 'switch' && parent_level_2_keyword === 'tasks') {
    return 'FLOW_TASK_SWITCH';
  }
  if(parent_level_1_keyword === 'switch') {
    return 'FLOW_TASK_SWITCH_CASE';
  }
  if(keyword === 'when' && parent_level_2_keyword === 'switch') {
    return 'FLOW_TASK_SWITCH_WHEN';
  }
  if(keyword === 'then' && parent_level_2_keyword === 'switch') {
    return 'FLOW_TASK_SWITCH_THEN';
  }
  
  // Decision Table / Asset

  if(keyword === 'form' && parent_level_2_keyword === 'tasks') {
    return 'FLOW_TASK_FORM';
  }
  if(keyword === 'ref' && parent_level_1_keyword === 'form') {
    return 'FLOW_TASK_FORM_REF';
  }
  if(keyword === 'returns' && parent_level_1_keyword === 'form') {
    return 'FLOW_TASK_FORM_RETURNS';
  }

  if(keyword === 'ref' && parent_level_3_keyword === 'tasks') {
    return 'FLOW_TASK_ASSET_REF';
  }
  if(keyword === 'collection' && parent_level_3_keyword === 'tasks') {
    return 'FLOW_TASK_ASSET_COLLECTION';
  }
  if(keyword === 'inputs' && parent_level_3_keyword === 'tasks') {
    return 'FLOW_TASK_ASSET_INPUTS';
  }
  if(parent_level_1_keyword === 'inputs' && parent_level_4_keyword === 'tasks') {
    return 'FLOW_TASK_ASSET_INPUT';
  }
  if(parent_level_2_keyword === 'tasks') {
    return 'FLOW_TASK_ASSET';
  }
  
  if(node.keyword === null) {
    return 'ROOT';
  }

  console.error(`Unclassified node: ${parentPath}`);
  return 'ROOT';
}