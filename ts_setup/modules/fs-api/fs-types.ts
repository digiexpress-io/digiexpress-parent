
export namespace Fs { }

export declare namespace Fs {

  export interface DirentBase {
    id: string;
    name: string;
    type: BodyType;
    fullPath: string;
    commitIndex: Index | undefined;
    children: DirentBase[];
    props: PropsBase | undefined;
  }

  export interface PropsBase {
    id: string;
    bodyType: BodyType;
    expanded: boolean;
    reference: boolean;
    locked: boolean;
    assetDescription?: string;
    configOptions: ConfigOption[];
    comments: Comment[];
    changes: Change[];
    permissions: Permission[];
    labels: DescriptionLabel[];
    errors: AssetError[];
  }

  export interface Index {
    objectId: string;
    treeId: string;
    createdBy: string;
    updatedBy: string;

    createdByAuthor: string;
    updatedByAuthor: string;

    createdAt: string;
    updatedAt: string;
  }

  export type BodyType =
    | 'LOCALE'
    | 'ARTICLE_LINK'
    | 'ARTICLE'
    | 'ARTICLE_WORKFLOW'
    | 'ARTICLE_PAGE'
    | 'ARTICLE_TEMPLATE' 
    | 'FLOW'
    | 'FLOW_TASK'
    | 'DECISION_TABLE'
    | 'DIALOB_FORM'
    | 'DIALOB_FORM_META'
    | 'PRINTOUT'
    | 'PRINTOUT_PAGE'
    | 'PRINTOUT_RESOURCE'
    | 'DEPLOYMENT'
    | 'FOLDER'
    | 'UNKNOWN';

  export type Props =
    | FolderProps
    | ArticleProps
    | WorkflowProps
    | DialobProps
    | DialobFormMetaProps
    | FlowProps
    | FlowTaskProps
    | DecisionTableProps
    | LanguageProps
    | PrintoutProps
    | PrintoutPageProps
    | PrintoutResourceProps
    | PageProps
    | TemplateProps
    | LinkProps
    | PhoneProps;

  // ------------- START WRENCH --------------

  export type ProgramStatus = 'UP' | 'ERROR';

  export type AstCommandValue =
    | 'SET_NAME'
    | 'SET_DESCRIPTION'
    | 'SET_HIT_POLICY'
    | 'ADD_HEADER_IN'
    | 'ADD_HEADER_OUT'
    | 'ADD_ROW'
    | 'SET_HEADER_TYPE'
    | 'SET_HEADER_REF'
    | 'SET_HEADER_NAME'
    | 'SET_HEADER_SCRIPT'
    | 'SET_HEADER_DIRECTION'
    | 'SET_HEADER_EXPRESSION'
    | 'SET_CELL_VALUE'
    | 'DELETE_CELL'
    | 'DELETE_HEADER'
    | 'DELETE_ROW'
    | 'MOVE_ROW'
    | 'MOVE_HEADER'
    | 'INSERT_ROW'
    | 'COPY_ROW'
    | 'IMPORT_CSV'
    | 'IMPORT_ORDERED_CSV'
    | 'SET_VALUE_SET'
    | 'ADD_LOG';

  export interface AstCommand {
    id?: string;
    value?: string;
    type: AstCommandValue;
  }

  export interface ModelError {
    id?: string;
    msg: string;
    line?: number;
    column?: number;
  }

  export interface ProgramAssociation {
    id?: string;
    ref: string;
    refType: BodyType;
    refStatus: ProgramStatus;
    owner: boolean;
  }

  export interface WrenchAstBody<T> {
    id: string;
    ast: T;
    errors: ModelError[];
    associations: ProgramAssociation[];
    status: ProgramStatus;
    commands: AstCommand[];
  }

  export interface FlowAst {
    parseTree: YamlFlow;
    headers: {
      acceptDefs: TypeDef[];
      returnDefs: TypeDef[];
    };
  }

  export type YamlFlowKeyword =
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
    | "form"
    | "debugValue";

  export interface YamlFlowInputType {
    name: string;
    value: string;
    ref?: string;
  }
  export interface YamlFlow extends Yaml {
    id: Yaml;
    description: Yaml;
    types: YamlFlowInputType[];
    inputs: Record<string, YamlFlowInputNode>;
    tasks: Record<string, YamlFlowTaskNode>;
  }
  export interface YamlFlowTaskNode extends Omit<Yaml, "switch"> {
    id: Yaml;
    order: number;
    then: Yaml;
    ref: YamlFlowRefNode;
    userTask: YamlFlowRefNode;
    decisionTable: YamlFlowRefNode;
    service: YamlFlowRefNode;
    form: YamlFlowRefNode;
    returns: YamlFlowRefNode;
    switch: Record<string, YamlFlowSwitchNode>;
  }
  export interface YamlFlowRefNode extends Yaml {
    ref: Yaml;
    collection: Yaml;
    inputsNode: Yaml;
    inputs: Record<string, Yaml>;
  }
  export interface YamlFlowSwitchNode extends Yaml {
    order: string;
    when: Yaml;
    then: Yaml;
  }
  export interface YamlFlowInputNode extends Yaml {
    required: Yaml;
    type: Yaml;
    debugValue: Yaml;
  }
  export interface Yaml {
    parent: Yaml | undefined;
    keyword: string;
    children: Record<string, Yaml>;
    value: string;
    source: { lineNumber: number, line: string };
    start: number;
    end: number;
    indent: number;

    collection?: Yaml | undefined;
    service?: Yaml | undefined;
    form?: Yaml | undefined;
    decisionTable?: Yaml | undefined;
    then?: Yaml | undefined;
    id?: Yaml | undefined;
    ref?: Yaml | undefined;
    returns?: Yaml | undefined;
    switch?: Yaml | undefined;
  }





  export interface FlowTaskAst {
    value: string;
    name: string;
    description: string | undefined;
    headers: {
      acceptDefs: TypeDef[];
      returnDefs: TypeDef[];
    };
  }

  export interface DecisionAstCell {
    id: string;
    header: string;
    value?: string;
  }

  export interface DecisionAstRow {
    id: string;
    order: number;
    cells: DecisionAstCell[];
  }

  export interface DecisionAst {
    name: string;
    description?: string;
    hitPolicy: 'ALL' | 'FIRST';
    headerTypes: string[];
    headerExpressions: Record<string, string[]>;
    headers: {
      acceptDefs: TypeDef[];
      returnDefs: TypeDef[];
    };
    rows: DecisionAstRow[];
  }

  export interface WrenchBody {
    flows: Record<string, WrenchAstBody<FlowAst>>;
    services: Record<string, WrenchAstBody<FlowTaskAst>>;
    decisions: Record<string, WrenchAstBody<DecisionAst>>;
  }

  export interface WrenchAstBodyChange {
    id: string;
    bodyType: BodyType;
    bodySyntax?: string;
    bodyStatment: AstCommand[]; // typo matches backend field name
  }

  export type ValueType = "TIME"
    | "DATE"
    | "DATE_TIME"
    | "INSTANT"
    | "PERIOD"
    | "DURATION"
    | "STRING"
    | "INTEGER"
    | "LONG"
    | "BOOLEAN"
    | "PERCENT"
    | "OBJECT"
    | "ARRAY"
    | "DECIMAL"
    | "MAP"
    | "FLOW_CONTEXT"
    | 'INTL';

  export type HitPolicy = "FIRST" | "ALL";

  export interface Headers {
    acceptDefs: TypeDef[];
    returnDefs: TypeDef[];
  }

  // ------------- END WRENCH --------------

  export type WorldFsBody = ArticlePageBody | WrenchBody | WrenchAstBody<FlowAst | FlowTaskAst | DecisionAst>;

  export interface FolderProps extends PropsBase {
    type: 'FOLDER';
  }

  export interface ArticleProps extends PropsBase {
    type: 'ARTICLE';
    orderNumber: number;
    devMode?: boolean;
  }

  export interface ArticlePageBody {
    content: string
  }

  export interface WorkflowProps extends PropsBase {
    type: 'ARTICLE_WORKFLOW';
    serviceName: string;
    dialobFormName: string;
    dialobFormTag: string;
    flowName: string;
    validityStart?: string;
    validityEnd?: string;
    articles: ArticleId[];
    configOptions: ConfigOption[];
    intlValues: Record<string, string>;
  }

  export interface DialobProps extends PropsBase {
    type: 'DIALOB_FORM';
    formName: string;
    formTechnicalId: string;
    versionTags?: string[];
  }

  export interface DialobFormMetaProps extends PropsBase {
    type: 'DIALOB_FORM_META';
    label: string;
    created: string;
    lastSaved: string;
    languages: string[];
  }

  export interface FlowProps extends PropsBase {
    type: 'FLOW';
    name: string;
  }

  export interface FlowTaskProps extends PropsBase {
    type: 'FLOW_TASK';
    taskName: string;
  }

  export interface DecisionTableProps extends PropsBase {
    type: 'DECISION_TABLE';
  }

  export interface LanguageProps extends PropsBase {
    type: 'LOCALE';
    localeCode: string;
    enabled: boolean;
  }

  export interface PageProps extends PropsBase {
    type: 'ARTICLE_PAGE';
    localeCode: string;
    articleId: string;
    content?: string;
  }

  export interface PrintoutProps extends PropsBase {
    type: 'PRINTOUT';
    printoutServiceName: string;
    orchestratorName: string;
    intlValues: Record<string, string>;
  }

  export interface PrintoutPageProps extends PropsBase {
    type: 'PRINTOUT_PAGE';
    content: string;
    localeId: string;
    serviceId: string;
    templateIds: string[];
  }

  export interface PrintoutResourceProps extends PropsBase {
    type: 'PRINTOUT_RESOURCE';
    externalLocation: string;
    resourceName: string;
    contentType: string;
    printoutPageIds: string[];
    content?: string;
  }

  export interface TemplateProps extends PropsBase {
    type: 'ARTICLE_TEMPLATE';
    content: string;
  }

  export interface LinkProps extends PropsBase {
    type: 'ARTICLE_LINK';
    urlValue: string;
    intlValues: Record<string, string>;
    articles: string[];
    contentType: LinkType;
    devMode?: boolean;
  }

  export type LinkType = 'internal' | 'external' | 'phone';

  export interface PhoneProps extends PropsBase {
    type: 'ARTICLE_LINK';
    phoneValue: string;
    intlValues: Record<string, string>;
  }

  export type ConfigOption = 'DEV_MODE' | 'ASSIGNABLE_MODE' | 'DISABLED_MODE' | 'ANONYMOUS_MODE' | 'AUTH_ONLY_MODE' | 'IN_HOUSE_MODE';

  export interface User {
    userName: string;
    email: string;
    permissions?: PermissionType[];
  }

  export interface AssetError {
    code: string;
    severity: ErrorSeverityType;
    message: string;
  }

  export interface Change {
    changeType: ChangeType;
    changeDate: string;
    changedBy: User;
  }

  export interface DescriptionLabel {
    key: string;
    text?: string;
  }

  export interface Permission {
    name: string;
    types: PermissionType[];
  }

  export interface Comment {
    comment: string;
    author: string;
    created: string;
  }

  export interface SelectOption {
    value: string;
    label: string;
  }

  export interface SelectOptions {
    articles: SelectOption[];
    flows: SelectOption[];
    dialobs: SelectOption[];
    languages: SelectOption[];
    printouts: SelectOption[];
    linkTypes: LinkType[];
    labels: string[];
    direntProps: Record<string, Fs.Props>;
    collectDialobTags: (dialobId: string) => SelectOption[];
    getActiveDialobTag: (props: DialobProps) => string;
  }

  export type SecondaryView = 'references' | 'properties' | 'debug' | 'preview' | 'history' | 'help' | 'errors' | 'changes' | 'article-order' | 'article-locale-overview' | 'stats';
  export type ChangeType = 'update' | 'create' | 'delete';
  export type PermissionType = 'read' | 'write' | 'view' | 'none';
  export type ErrorSeverityType = 'CRITICAL' | 'WARNING';

  export type FolderId = string;
  export type ArticleId = string;
  export type LanguageId = string;
  export type WorkflowId = string;
  export type DialobId = string;
  export type FlowId = string;
  export type LinkId = string;
  export type PrintoutId = string;
  export type ImageId = string;
  export type TemplateId = string;
  export type PhoneNumberId = string;

  export interface ContextMenuData {
    dirent: DirentBase;
    anchorPosition: { top: number; left: number };
  }

  // ------------- DEBUG TYPES --------------

  export type DebugInputType = 'JSON' | 'CSV';

  export interface TypeDef {
    id: string;
    name: string;
    order: number;
    data: boolean;
    direction: 'IN' | 'OUT';
    valueType: ValueType;
    required: boolean;
    properties: TypeDef[];
    values?: string;
    valueSet?: string[];
    expression?: string;
    script?: string;
  }

  export interface DebugResponse {
    body?: DecisionResult | ServiceResult | FlowResult;
    bodyCsv?: string;
  }

  export interface DecisionLogEntry {
    match: boolean;
    headerType: TypeDef;
    expression: string;
    usedValue?: any;
  }

  export interface DecisionLog {
    match: boolean;
    order: number;
    accepts: DecisionLogEntry[];
    returns: DecisionLogEntry[];
  }

  export interface DecisionResult {
    rejections: DecisionLog[];
    matches: DecisionLog[];
  }

  export interface ServiceResult {
    value: any;
  }

  export type FlowExecutionStatus = 'COMPLETED' | 'ERROR';

  export interface FlowResultErrorLog {
    id: string;
    msg: string;
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

  export interface FlowResult {
    logs: FlowResultLog[];
  }

  export interface CsvEntry {
    name: string;
    value: string;
  }

  export interface CsvRow {
    id: string;
    inputs: CsvEntry[];
    outputs: CsvEntry[];
  }

  export interface ServiceErrorMsg {
    id: string;
    value: string;
  }

  export interface StoreError {
    text: string;
    status: number;
    errors: ServiceErrorMsg[];
  }
}
