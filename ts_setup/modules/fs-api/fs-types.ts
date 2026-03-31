
export namespace Fs { }

export declare namespace Fs {

  export interface DirentBase {
    id: string;
    name: string;
    type: Fs.Type;
    children: DirentBase[];
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

  export type Type = keyof PropsMap;
  export type Props = PropsMap[Type];
  export type DirentAsset = DirentBase & Props;

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

  export interface PropsBase {
    id: string;
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

  export interface FolderProps extends PropsBase {
    type: 'folder';
  }

  export interface ArticleProps extends PropsBase {
    type: 'article';
    orderNumber: number;
  }

  export interface ServiceProps extends PropsBase {
    type: 'service';
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
    type: 'dialob';
    formName: string;
    formTechnicalId: string;
    versionTags?: string[];
  }

  export interface FlowProps extends PropsBase {
    type: 'flow';
    name: string;
  }

  export interface LanguageProps extends PropsBase {
    type: 'language';
    localeCode: string;
  }

  export interface PageProps extends PropsBase {
    type: 'page';
    localeCode: string;
    articleId: string;
    content?: string;
  }

  export interface PrintoutProps extends PropsBase {
    type: 'printout';
    printoutServiceName: string;
    orchestratorName: string;
    intlValues: Record<string, string>;
  }

  export interface ImageProps extends PropsBase {
    type: 'image';
  }

  export interface TemplateProps extends PropsBase {
    type: 'template';
    serviceId: string;
    localeId: string;
    content?: string;
  }

  export interface LinkProps extends PropsBase {
    type: 'link';
    urlValue: string;
    intlValues: Record<string, string>;
  }

  export interface PhoneProps extends PropsBase {
    type: 'phone';
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
    direntProps: Record<string, Fs.Props>;
    collectDialobTags: (dialobId: string) => SelectOption[];
    getActiveDialobTag: (props: DialobProps) => string;
  }

  export type SecondaryView = 'references' | 'properties' | 'configuration' | 'debug' | 'preview' | 'history' | 'help' | 'errors' | 'changes' | 'article-order';
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
