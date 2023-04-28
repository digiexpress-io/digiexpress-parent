export type TagId = string;
export type EntityId = string;
export type FlowId = string;
export type ServiceId = string;
export type DecisionId = string;
export type AstBodyType = "FLOW" | "FLOW_TASK" | "DT" | "TAG";
export type Direction = "IN" | "OUT";
export type ValueType = "TIME" | "DATE" | "DATE_TIME" | "INSTANT" | "PERIOD" | "DURATION" |
  "STRING" | "INTEGER" | "LONG" | "BOOLEAN" | "PERCENT" | "OBJECT" | "ARRAY" | "DECIMAL" | 
  "MAP" | "FLOW_CONTEXT";
export type ProgramStatus = "UP" | "AST_ERROR" | "PROGRAM_ERROR" | "DEPENDENCY_ERROR";

export type HitPolicy = "FIRST" | "ALL";
export type AssociationType = "ONE_TO_ONE" | "ONE_TO_MANY";
export type ColumnExpressionType = "IN" | "EQUALS";
export type FlowCommandMessageType = "ERROR" | "WARNING";

// decision table
export interface AstDecision extends AstBody {
  headerTypes: string[];
  headerExpressions: Record<ValueType, string[]>;
  hitPolicy: HitPolicy;
  rows: AstDecisionRow[];
}
export interface AstDecisionRow {
  id: string;
  order: number;
  cells: AstDecisionCell[];
}
export interface AstDecisionCell {
  id: string;
  header: string;
  value?: string;
}



// flow
export interface AstFlow extends AstBody {
  src: AstFlowRoot;
  messages: FlowAstCommandMessage[];
}
export interface FlowAstCommandMessage {
  line: number;
  value: string;
  type: FlowCommandMessageType;
  range?: FlowAstCommandRange;
}
export interface FlowAstCommandRange {
  start: number;
  end: number;
}
export interface AstFlowInputType {
  name: string;
  value: string;
  ref?: string;
}
export interface AstFlowRoot extends AstFlowNode {
  id: AstFlowNode;
  description: AstFlowNode;
  types: AstFlowInputType[];
  inputs: Record<string, AstFlowInputNode>;
  tasks: Record<string, AstFlowTaskNode>;
}
export interface AstFlowTaskNode extends AstFlowNode {
  id: AstFlowNode;
  order: number;
  then: AstFlowNode;
  ref: AstFlowRefNode;
  userTask: AstFlowRefNode;
  decisionTable: AstFlowRefNode;
  service: AstFlowRefNode;
  switch: Record<string, AstFlowSwitchNode>;
}
export interface AstFlowRefNode extends AstFlowNode {
  ref: AstFlowNode;
  collection: AstFlowNode;
  inputsNode: AstFlowNode;
  inputs: Record<string, AstFlowNode>;
}
export interface AstFlowSwitchNode extends AstFlowNode {
  order: string;
  when: AstFlowNode;
  then: AstFlowNode;
}
export interface AstFlowInputNode extends AstFlowNode {
  required: AstFlowNode;
  type: AstFlowNode;
  debugValue: AstFlowNode;
}
export interface AstFlowNode {
  parent: AstFlowNode;
  keyword: string;
  children: Record<string, AstFlowNode>;
  value: string;
  source: AstChangeset;
  start: number;
  end: number;
}
export interface AstChangeset {
  line: number;
  value: string;
}



// Service
export interface AstService extends AstBody {
  executorType: "TYPE_0" | "TYPE_1" | "TYPE_2";
  value: string
}
// Tag
export interface AstTag extends AstBody {
  name: string;
  created: string;
}

export interface AstTagValue {
  hash: string;
  bodyType: AstBodyType;
} 

export interface TypeDef {
  id: string; // GID
  name: string;
  order: number;
  data: boolean;

  direction: Direction;
  valueType: ValueType;
  required: boolean;
  properties: TypeDef[];
  values?: string;
  valueSet?: string[];
}
export interface AstBody {
  name: string;
  description?: string;
  headers: Headers;
  bodyType: AstBodyType;
}

export interface Headers {
  acceptDefs: TypeDef[];
  returnDefs: TypeDef[];
}

export interface AstSource {
  id: string;
  hash: string;
  bodyType: AstBodyType;
}
export interface ProgramAssociation {
  id?: string;
  ref: string;
  refType: AstBodyType;
  refStatus: ProgramStatus;
  owner: string;
}


export interface ProgramMessage {
  id: string;
  msg: string;
}
export interface Entity<A extends AstBody> {
  id: EntityId
  ast?: A;
  source: AstSource;
  warnings: ProgramMessage[];
  errors: ProgramMessage[];
  associations: ProgramAssociation[];
  status: ProgramStatus;
}
export interface Site {
  name: string,
  contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION",
  tags: Record<TagId, Entity<AstTag>>;
  flows: Record<FlowId, Entity<AstFlow>>;
  services: Record<ServiceId, Entity<AstService>>;
  decisions: Record<DecisionId, Entity<AstDecision>>;
}
