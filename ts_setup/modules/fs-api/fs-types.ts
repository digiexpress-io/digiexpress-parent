export interface FsDirent {
  id: string;
  name: string;
  type: FsDirentType;
  children: FsDirent[];
}

export interface BaseDirentProps {
  id: string;
  expanded: boolean;
  reference: boolean;
  locked: boolean;
  description?: string;
  configOptions: FsDirentConfigOption[];
  comments: FsDirentComment[];
  changes: FsDirentChange[];
  permissions: FsDirentPermission[];
  labels: FsDirentLabel[];
  errors: FsDirentError[];
}

export interface FolderDirentProps extends BaseDirentProps {
  type: 'folder';
}

export interface ArticleDirentProps extends BaseDirentProps {
  type: 'article';
  orderNumber: number;
}

export interface ServiceDirentProps extends BaseDirentProps {
  type: 'service';
  serviceName: string;
  dialobFormName: string;
  dialobFormTag: string;
  flowName: string;
  validityStart?: string;
  validityEnd?: string;
  articles: ArticleId[];
  configOptions: FsDirentConfigOption[];
  intlValues: Record<string, string>;
}

export interface DialobDirentProps extends BaseDirentProps {
  type: 'dialob';
  formName: string; // reader-friendly form name
  formTechnicalId: string; // user-created id
  versionTags?: string[];
}

export interface FlowDirentProps extends BaseDirentProps {
  type: 'flow';
}

export interface LanguageDirentProps extends BaseDirentProps {
  type: 'language';
  localeCode: string;
}

export interface PageDirentProps extends BaseDirentProps {
  type: 'page';
  localeCode: string;
  articleId: string;
  content?: string;
}

export interface PrintoutDirentProps extends BaseDirentProps {
  type: 'printout';
  printoutServiceName: string;
  orchestratorName: string;
  intlValues: Record<string, string>;
}

export interface ImageDirentProps extends BaseDirentProps {
  type: 'image';
}
export interface TemplateDirentProps extends BaseDirentProps {
  type: 'template';
  serviceId: string;
  localeId: string;
  content?: string;
}
export interface LinkDirentProps extends BaseDirentProps {
  type: 'link';
  urlValue: string;
  intlValues: Record<string, string>;
}
export interface PhoneDirentProps extends BaseDirentProps {
  type: 'phone';
  phoneValue: string;
  intlValues: Record<string, string>;
}

export type FsDirentProps =
  | FolderDirentProps
  | ArticleDirentProps
  | ServiceDirentProps
  | DialobDirentProps
  | FlowDirentProps
  | LanguageDirentProps
  | PrintoutDirentProps
  | ImageDirentProps
  | PageDirentProps
  | TemplateDirentProps
  | LinkDirentProps
  | PhoneDirentProps;

export type FsDirentEntry = FsDirent & FsDirentProps;
export type FolderEntry = FsDirent & FolderDirentProps;
export type ArticleEntry = FsDirent & ArticleDirentProps;
export type ServiceEntry = FsDirent & ServiceDirentProps;
export type DialobEntry = FsDirent & DialobDirentProps;
export type FlowEntry = FsDirent & FlowDirentProps;
export type LanguageEntry = FsDirent & LanguageDirentProps;
export type PageEntry = FsDirent & PageDirentProps;
export type PrintoutEntry = FsDirent & PrintoutDirentProps;
export type ImageEntry = FsDirent & ImageDirentProps;
export type TemplateEntry = FsDirent & TemplateDirentProps;
export type LinkEntry = FsDirent & LinkDirentProps;
export type PhoneEntry = FsDirent & PhoneDirentProps;

export type FsDirentConfigOption = 'devMode' | 'assignableMode' | 'disabledMode' | 'anonymousMode';

export interface User {
  userName: string;
  email: string;
  permissions?: FsDirentPermissionType[];
}

export interface FsDirentError {
  code: string;
  severity: FsDirentErrorSeverityType;
  message: string;
}

export interface FsDirentChange {
  changeType: FsDirentChangeType;
  changeDate: string;
  changedBy: User;
}

export interface FsDirentLabel {
  id: FsDirentLabelId;
  value: string;
}

export interface FsDirentPermission {
  name: string;
  types: FsDirentPermissionType[];
}

export interface FsDirentComment {
  comment: string;
  author: string;
  created: string;
}

export const FsDirentTypes = {
  folder: 'folder',
  article: 'article',
  service: 'service',
  dialob: 'dialob',
  flow: 'flow',
  link: 'link',
  language: 'language',
  printout: 'printout',
  image: 'image',
  template: 'template',
  phone: 'phone',
  page: 'page'
};

export type FsDirentType = keyof typeof FsDirentTypes;
export type FsDirentSecondaryView = 'references' | 'properties' | 'configuration' | 'debug' | 'preview' | 'history' | 'help' | 'errors' | 'changes' | 'article-order';
export type FsDirentChangeType = 'update' | 'create' | 'delete';
export type FsDirentPermissionType = 'read' | 'write' | 'view' | 'none';
export type FsDirentErrorSeverityType = 'CRITICAL' | 'WARNING';

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
export type FsDirentLabelId = string;
export type PhoneNumberId = string;

export interface FsDirentContextMenuData {
  dirent: FsDirent;
  anchorPosition: { top: number; left: number };
}