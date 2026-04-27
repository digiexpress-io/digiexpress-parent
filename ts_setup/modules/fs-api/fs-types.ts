
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

  export interface PropsMap {
    folder: FolderProps;
    article: ArticleProps;
    service: ServiceProps;
    dialob: DialobProps;
    flow: FlowProps;
    language: LanguageProps;
    printout: PrintoutProps;
    image: ImageProps;
    page: PageProps;
    template: TemplateProps;
    link: LinkProps;
    phone: PhoneProps;
  }

  export type BodyType =
    | 'LOCALE'
    | 'ARTICLE_LINK'
    | 'ARTICLE'
    | 'ARTICLE_WORKFLOW'
    | 'ARTICLE_PAGE'
    | 'ARTICLE_TEMPLATE' // confusion with PRINTOUT_TEMPLATE
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

  export type Type = keyof PropsMap;
  export type Props = PropsMap[Type];


  export type Folder = DirentBase & FolderProps;
  export type Article = DirentBase & ArticleProps;
  export type Service = DirentBase & ServiceProps;
  export type Dialob = DirentBase & DialobProps;
  export type Flow = DirentBase & FlowProps;
  export type Language = DirentBase & LanguageProps;
  export type Printout = DirentBase & PrintoutProps;
  export type Image = DirentBase & ImageProps;
  export type Page = DirentBase & PageProps;
  export type Template = DirentBase & TemplateProps;
  export type Link = DirentBase & LinkProps;
  export type Phone = DirentBase & PhoneProps;

  export type Dirent = Folder | Article | Service | Dialob | Flow | Language
    | Printout | Image | Page | Template | Link | Phone;

  export interface FolderProps extends PropsBase {
    type: 'FOLDER';
  }

  export interface ArticleProps extends PropsBase {
    type: 'ARTICLE';
    orderNumber: number;
  }

  export interface ServiceProps extends PropsBase {
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

  export interface ImageProps extends PropsBase {
    type: 'image';
  }

  export interface TemplateProps extends PropsBase {
    type: 'ARTICLE_TEMPLATE';
    printoutServiceId: string;
    localeId: string;
    content?: string;
  }

  export interface LinkProps extends PropsBase {
    type: 'ARTICLE_LINK';
    urlValue: string;
    intlValues: Record<string, string>;
  }

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
    languages: string[];
    labels: string[];
    direntProps: Record<string, Fs.Props>;
    collectDialobTags: (dialobId: string) => SelectOption[];
    getActiveDialobTag: (props: DialobProps) => string;
  }

  export type SecondaryView = 'references' | 'properties' | 'configuration' | 'debug' | 'preview' | 'history' | 'help' | 'errors' | 'changes' | 'article-order' | 'overview';
  export type ChangeType = 'update' | 'create' | 'delete';
  export type PermissionType = 'read' | 'write' | 'view' | 'none';
  export type ErrorSeverityType = 'CRITICAL' | 'WARNING';

  export type FolderId = string;
  export type ArticleId = string;
  export type LanguageId = string;
  export type ServiceId = string;
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
