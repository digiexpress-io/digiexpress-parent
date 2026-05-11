
export namespace Fs { }

export declare namespace Fs {

  export interface DirentBase {
    id: string;
    name: string;
    type: BodyType;
    fullPath: string;
    children: DirentBase[];
    props: PropsBase | undefined;
  }

  export interface PropsBase {
    id: string;
    bodyType: BodyType;
    expanded: boolean;
    reference: boolean;
    locked: boolean;
    description?: string;
    configOptions: ConfigOption[];
    comments: Comment[];
    changes: Change[];
    permissions: Permission[];
    labels: Label[];
    errors: AssetError[];
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
    | FlowProps
    | FlowTaskProps
    | LanguageProps
    | PrintoutProps
    | PrintoutPageProps
    | PrintoutResourceProps
    | PageProps
    | TemplateProps
    | LinkProps
    | PhoneProps;

  // ------------- START WRENCH --------------

  export type WrenchProgramStatus = 'UP' | 'ERROR';

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

  export interface WrenchModelError {
    id?: string;
    msg: string;
    line?: number;
    column?: number;
  }

  export interface WrenchProgramAssociation {
    id?: string;
    ref: string;
    refType: BodyType;
    refStatus: WrenchProgramStatus;
    owner: boolean;
  }

  export interface WrenchAstBody<T> {
    id: string;
    ast: T;
    errors: WrenchModelError[];
    associations: WrenchProgramAssociation[];
    status: WrenchProgramStatus;
    commands: AstCommand[];
  }

  export interface FlowAst {
    parseTree?: {
      value: string;
    };
  }

  export interface FlowTaskAst {
    value: string;
  }

  export interface DecisionTypeDef {
    id: string;
    name: string;
    order: number;
    direction: 'IN' | 'OUT';
    valueType: string;
    valueSet?: string[];
    expression?: string;
    script?: string;
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
      acceptDefs: DecisionTypeDef[];
      returnDefs: DecisionTypeDef[];
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

  export type AstBodyType = "FLOW"
    | "FLOW_TASK"
    | "DECISION_TABLE"
    | "TAG"
    | "BRANCH";

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
    acceptDefs: DecisionTypeDef[];
    returnDefs: DecisionTypeDef[];
  }

  export interface AstBody {
    name: string;
    description?: string;
    headers: Headers;
    bodyType: AstBodyType;
    parseTree?: { value: string }
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

  // ------------- END WRENCH --------------

  export type WorldFsBody = ArticlePageBody | WrenchBody | WrenchAstBody<FlowAst | FlowTaskAst | DecisionAst>;

  export interface FolderProps extends PropsBase {
    type: 'FOLDER';
  }

  export interface ArticleProps extends PropsBase {
    type: 'ARTICLE';
    orderNumber: number;
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

  export interface FlowProps extends PropsBase {
    type: 'FLOW';
    name: string;
    content?: string;
  }

  export interface FlowTaskProps extends PropsBase {
    type: 'FLOW_TASK';
    taskName: string;
    taskValue: string;
  }

  export interface LanguageProps extends PropsBase {
    type: 'LOCALE';
    localeCode: string;
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
    templateIds: string[];
    content?: string;
  }

  export interface TemplateProps extends PropsBase {
    type: 'ARTICLE_TEMPLATE';
    content?: string;
  }

  export interface LinkProps extends PropsBase {
    type: 'ARTICLE_LINK';
    urlValue: string;
    intlValues: Record<string, string>;
    articles: string[];
    contentType: LinkType;
  }

  export type LinkType = 'internal' | 'external' | 'phone';

  export interface PhoneProps extends PropsBase {
    type: 'ARTICLE_LINK';
    phoneValue: string;
    intlValues: Record<string, string>;
  }

  export type ConfigOption = 'devMode' | 'assignableMode' | 'disabledMode' | 'anonymousMode';

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

  export interface Label {
    id: LabelId;
    value: string;
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
  export type LabelId = string;
  export type PhoneNumberId = string;

  export interface ContextMenuData {
    dirent: DirentBase;
    anchorPosition: { top: number; left: number };
  }
}
