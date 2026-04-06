export namespace HdesApi {
  export const BLOCK_TYPES: NodeKeywordTypes[] = ['tasks', 'inputs', 'decisionTable', 'service', 'switch', 'returns' ];

  const getErrorMsg = (error: any) => {
    if (error.msg) {
      return error.msg;
    }
    if (error.value) {
      return error.value
    }
    if (error.message) {
      return error.message;
    }
  }
  const getErrorId = (error: any) => {
    if (error.id) {
      return error.id;
    }
    if (error.code) {
      return error.code
    }
    return "";
  }
  export const parseErrors = (props: any[]): HdesApi.ServiceErrorMsg[] => {
    if (!props) {
      return []
    }
  
    const result: HdesApi.ServiceErrorMsg[] = props.map(error => ({
      id: getErrorId(error),
      value: getErrorMsg(error)
    }));
  
    return result;
  }

  export class ServiceImpl implements HdesApi.Service {
    private _api: ServiceRestApi;
    private _branchName: string | undefined;

    constructor(props: ServiceRestApi, branchName?: string | undefined) {
      this._api = props;
      this._branchName = branchName;
    }

    get branchName() {
      return this._branchName;
    }

    withBranchName(branchName: string | undefined) {
      return new ServiceImpl(this._api, branchName === 'default' ? undefined : branchName)
    }

    create(): HdesApi.CreateBuilder {
      const flow = (name: string) => this.createAsset(name, undefined, "FLOW");
      const service = (name: string) => this.createAsset(name, undefined, "FLOW_TASK");
      const decision = (name: string) => this.createAsset(name, undefined, "DECISION_TABLE");
      const branch = (body: HdesApi.AstCommand[]) => this.createAsset("branch", undefined, "BRANCH", body);
      const tag = (props: {name: string, desc: string}) => this.createAsset(props.name, props.desc, "TAG");
      const site = () => this.createAsset("repo", undefined, "SITE");
      
      const importData = (tagContentAsString: string): Promise<HdesApi.Site> => {
        return this._api.importTag(tagContentAsString)
      }
      
      return { flow, service, decision, branch, site, tag, importData };
    }
    delete(): HdesApi.DeleteBuilder {
      const deleteMethod = (id: string): Promise<HdesApi.Site> => this._api.remove(id, this._branchName);
      const flow = (id: HdesApi.FlowId) => deleteMethod(id);
      const service = (id: HdesApi.ServiceId) => deleteMethod(id);
      const decision = (id: HdesApi.DecisionId) => deleteMethod(id);
      const branch = (id: HdesApi.BranchId) => deleteMethod(id);
      const tag = (id: HdesApi.TagId) => deleteMethod(id);
      return { flow, service, decision, tag, branch };
    }
    ast(id: string, bodyType: HdesApi.AstBodyType, body: HdesApi.AstCommand[]): Promise<HdesApi.Entity<any>> {
      return this._api.ast(id, bodyType, body, this._branchName);
    }
    update(id: string, bodyType: HdesApi.AstBodyType, body: HdesApi.AstCommand[]): Promise<HdesApi.Site> {
      return this._api.update(id, bodyType, body, this._branchName);
    }
    createAsset(name: string, desc: string | undefined, type: HdesApi.AstBodyType | "SITE", body?: HdesApi.AstCommand[]): Promise<HdesApi.Site> {
      return this._api.createAsset(name, desc, type, body, this._branchName);
    }

    getSite(): Promise<HdesApi.Site> {
      return this._api.getSite(this._branchName);
    }
    getSiteCommitLog(): Promise<HdesApi.CommitLog[]> {
      return this._api.getSiteCommitLog();
    }
    debug(debug: HdesApi.DebugRequest): Promise<HdesApi.DebugResponse> {
      return this._api.debug(debug, this._branchName);
    }
    copy(id: string, name: string): Promise<HdesApi.Site> {
      return this._api.copy(id, name, this._branchName);
    }
    diff(input: HdesApi.DiffRequest): Promise<HdesApi.DiffResponse> {
      return this._api.diff(input);
    }
    summary(tagId: string): Promise<HdesApi.AstTagSummary> {
      return this._api.summary(tagId);
    }
  }
}



export declare namespace HdesApi {
  export type NodeKeywordTypes = 
    | "id"
    | "description"
    | "inputs"
    | "returns"
    | "tasks"
    | "then"
    | "when"
    | "switch"
    | "required"
    | "type"
    | "decisionTable"
    | "userTask"
    | "ref"
    | "collection"
    | "service"
    | "debugValue";


  export type TagId = string;
  export type EntityId = string;
  export type FlowId = string;
  export type ServiceId = string;
  export type DecisionId = string;
  export type BranchId = string;
  export type AstBodyType = "FLOW" | "FLOW_TASK" | "DECISION_TABLE" | "TAG" | "BRANCH";
  export type Direction = "IN" | "OUT";
  export type ValueType = "TIME" | "DATE" | "DATE_TIME" | "INSTANT" | "PERIOD" | "DURATION" |
    "STRING" | "INTEGER" | "LONG" | "BOOLEAN" | "PERCENT" | "OBJECT" | "ARRAY" | "DECIMAL" | 
    "MAP" | "FLOW_CONTEXT" | 'INTL';
  export type ProgramStatus = "UP" | "AST_ERROR" | "PROGRAM_ERROR" | "DEPENDENCY_ERROR";

  export type HitPolicy = "FIRST" | "ALL";
  export type AssociationType = "ONE_TO_ONE" | "ONE_TO_MANY";
  export type ColumnExpressionType = "IN" | "EQUALS";
  export type FlowCommandMessageType = "ERROR" | "WARNING";
  export type AstCommandValue = (
    // DT related
    "SET_NAME" | "SET_DESCRIPTION" | "IMPORT_CSV" | "IMPORT_ORDERED_CSV" |
    "MOVE_ROW" | "MOVE_HEADER" | "INSERT_ROW" | "COPY_ROW" |
    "SET_HEADER_TYPE" | "SET_HEADER_REF" | "SET_HEADER_NAME" |
    "SET_HEADER_SCRIPT" | "SET_HEADER_DIRECTION" | "SET_HEADER_EXPRESSION" | "SET_HIT_POLICY" | "SET_CELL_VALUE" |
    "DELETE_CELL" | "DELETE_HEADER" | "DELETE_ROW" |
    "ADD_LOG" | "ADD_HEADER_IN" | "ADD_HEADER_OUT" | "ADD_ROW" |
    "SET_VALUE_SET"
  );
  export interface CommandsAndChanges {
    commands: AstCommand[];
    src: AstChangeset[];
  }
  export interface AstChangeset {
    line: number;
    value: string;
    commands: AstCommand[];
  }

  export interface ProgramMessage {
    id: string;
    msg: string;
    exception?: { column?: number, row?: number }
  }
  export interface AstCommand {
    id?: string;
    value?: string;
    type: AstCommandValue;
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
    parseTree?: { value: string }
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



  export interface CommitLog {
    objectId: string;
    commitId: string;
    createdAt: string;
    createdBy: string;
  }

  export interface Site {
    name: string,
    contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION",
    tags: Record<TagId, Entity<AstTag>>;
    flows: Record<FlowId, Entity<AstFlow>>;
    services: Record<ServiceId, Entity<AstService>>;
    decisions: Record<DecisionId, Entity<AstDecision>>;
    branches: Record<BranchId, Entity<AstBranch>>;
  }

  export interface Entity<A extends AstBody> {
    id: EntityId
    ast?: A;
    commands: AstCommand[];
    source: AstSource;
    warnings: ProgramMessage[];
    errors: ProgramMessage[];
    associations: ProgramAssociation[];
    status: ProgramStatus;
  }

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
    parseTree: AstFlowRoot;
    errors: FlowAstCommandMessage[];
  }
  export interface FlowAstCommandMessage {
    line: number;
    column: number | undefined;
    msg: string;
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
  export interface AstFlowTaskNode extends Omit<AstFlowNode, "switch"> {
    id: AstFlowNode;
    order: number;
    then: AstFlowNode;
    ref: AstFlowRefNode;
    userTask: AstFlowRefNode;
    decisionTable: AstFlowRefNode;
    service: AstFlowRefNode;
    returns: AstFlowRefNode;
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
    parent: AstFlowNode | undefined;
    keyword: string;
    children: Record<string, AstFlowNode>;
    value: string;
    source: AstChangeset;
    start: number;
    end: number;
    indent: number;

    collection?: AstFlowNode | undefined;
    service?: AstFlowNode | undefined;
    decisionTable?: AstFlowNode | undefined;
    then?: AstFlowNode | undefined;
    id?: AstFlowNode | undefined;
    ref?: AstFlowNode | undefined;
    returns?: AstFlowNode | undefined;
    switch?: AstFlowNode | undefined;
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
    values: AstTagValue[];
  }

  export interface AstTagValue {
    hash: string;
    bodyType: AstBodyType;
    commands: AstCommand[];
  }

  export interface AstBranch extends AstBody {
    name: string;
    created: string;
    tagId: string;
  }

  export interface ServiceErrorMsg {
    id: string;
    value: string;
  }
  export interface ServiceErrorProps {
    text: string;
    status: number;
    errors: ServiceErrorMsg[];
  }


  // Debug related

  export interface DebugRequest {
    id: string;
    input?: string;
    inputCSV?: string;
  }
  export interface DebugResponse {
    id: string;
    body?: ProgramResult;
    bodyCsv?: string;
  }  
  export interface ProgramResult {}  

  export interface ServiceResult extends ProgramResult { value: any; }
    
  export interface DecisionResult extends ProgramResult {
    rejections: DecisionLog[];
    matches: DecisionLog[];
  }
  export interface DecisionLog  {
    match: boolean;
    order: number;
    accepts: DecisionLogEntry[];
    returns: DecisionLogEntry[];
  }
  export interface DecisionLogEntry {
    match: boolean;
    headerType: TypeDef;
    expression: string;
    usedValue?: any;
  }

  export type FlowProgramStepPointerType = "SWITCH" | "THEN" | "END";
  export type FlowProgramStepRefType = "SERVICE" | "DT" 
  export type FlowExecutionStatus = "COMPLETED" | "ERROR";
  export interface FlowResult extends ProgramResult {
    stepId: string;
    shortHistory: string;
    logs: FlowResultLog[];
    status: FlowExecutionStatus;
    accepts: Record<string, any>;
    returns: Record<string, any>;
  }
  export interface FlowResultLog {
    id: number;
    stepId: string;
    start: string | Date; 
    end: string | Date;
    errors: FlowResultErrorLog[];
    status: FlowExecutionStatus;
    accepts: Record<string, any>;
    returns: Record<string, any>;
  }
  export interface FlowResultErrorLog { id: string; msg: string; }

  export interface Input {
    name: string | null | undefined;
    value: string | null | undefined
  }

  export interface Output {
    name: string | null | undefined;
    value: string | null | undefined
  }

  export interface CsvRow {
    id: string;
    inputs: Input[];
    outputs: Output[]
  }


  export interface CreateBuilder {
    site(): Promise<Site>;
    importData(init: string): Promise<Site>;
    tag(props: {name: string, desc: string}): Promise<Site>;
    flow(name: string): Promise<Site>;
    service(name: string): Promise<Site>;
    decision(name: string): Promise<Site>;
    branch(body: AstCommand[]): Promise<Site>;
  }

  export interface DeleteBuilder {
    tag(tagId: TagId): Promise<Site>;
    flow(flowId: FlowId): Promise<Site>;
    service(serviceId: ServiceId): Promise<Site>;
    decision(decisionId: DecisionId): Promise<Site>;
    branch(branchId: BranchId): Promise<Site>;
  }

  export interface DiffRequest {
    baseId: string;
    targetId: string;
  }

  export interface DiffResponse {
    baseName: string;
    targetName: string;
    baseId: string;
    targetId: string;
    created: string;
    body: string;
  }

  export interface AstTagSummaryEntity {
    id: string;
    name: string;
    body: string;
  }

  export interface AstTagSummary {
    tagName: string;
    flows: AstTagSummaryEntity[];
    services: AstTagSummaryEntity[];
    decisions: AstTagSummaryEntity[];
  }

  export interface VersionEntity {
    version: string;
    built: string;
  }

  export interface Service {
    branchName: string | undefined;
    delete(): DeleteBuilder;
    create(): CreateBuilder;
    
    update(id: string, bodyType: HdesApi.AstBodyType, body: AstCommand[] | string): Promise<Site>;
    ast(id: string, bodyType: HdesApi.AstBodyType, body: AstCommand[] | string): Promise<Entity<any>>;

    debug(input: DebugRequest): Promise<DebugResponse>;
    getSite(): Promise<Site>
    getSiteCommitLog(): Promise<CommitLog[]>
    copy(id: string, name: string): Promise<Site>
    diff(input: DiffRequest): Promise<DiffResponse>
    summary(tagId: string): Promise<AstTagSummary>
    withBranchName(branchName: string | undefined): Service;
  }

  export interface StoreError extends Error {
    text: string;
    status: number;
    errors: ServiceErrorMsg[];
  }

  export interface StoreConfig {
    url: string;
    oidc?: string;
    status?: string;
    csrf?: { key: string, value: string }
  }

  export interface ServiceRestApi {
    
    update(
      id: string, 
      bodyType: HdesApi.AstBodyType, 
      body: HdesApi.AstCommand[] | string, 
      branchName: string | undefined
    ): Promise<HdesApi.Site>;
    
    ast(
      id: string, 
      bodyType: HdesApi.AstBodyType, 
      body: HdesApi.AstCommand[] | string, 
      branchName: string | undefined
    ): Promise<HdesApi.Entity<any>>;

    createAsset(name: string, desc: string | undefined, type: HdesApi.AstBodyType | "SITE", body: HdesApi.AstCommand[] | undefined, branchName: string | undefined): Promise<HdesApi.Site>;
    getSite(branchName: string | undefined): Promise<HdesApi.Site>;
    getSiteCommitLog(): Promise<HdesApi.CommitLog[]>;
    debug(debug: HdesApi.DebugRequest, branchName: string | undefined): Promise<HdesApi.DebugResponse>;
    copy(id: string, name: string, branchName: string | undefined): Promise<HdesApi.Site>;
    diff(input: HdesApi.DiffRequest): Promise<HdesApi.DiffResponse>;
    summary(tagId: string): Promise<HdesApi.AstTagSummary>;
    remove(id: string, branchName: string | undefined): Promise<HdesApi.Site>;
    importTag(tagContentAsString: string): Promise<HdesApi.Site>;
  }

}