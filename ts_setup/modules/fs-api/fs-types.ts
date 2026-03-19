// Mock data structure for Fs component
export interface FsDirent {
  id: string;
  name: string;
  description?: string;
  expanded: boolean;
  reference: boolean;
  locked: boolean;
  type: FsDirentType;
  children: FsDirent[];
  configOptions: FsDirentConfigOption[];
  comments: FsDirentComment[];
  changes: FsDirentChange[];
  permissions: FsDirentPermission[];
  labels: FsDirentLabel[];
  errors: FsDirentError[];
}

export interface FsDirentConfigOption {
  devMode?: boolean | undefined;
  assignableMode?: boolean | undefined;
  disabledMode?: boolean | undefined;
  anonymousMode?: boolean | undefined;
}

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

export type FsDirentType = 'folder' | 'article' | 'service' | 'dialob' | 'flow' | 'link' | 'language' | 'printout' | 'image' | 'template';
export type FsDirentSecondaryView = 'references' | 'properties' | 'configuration' | 'debug' | 'preview' | 'history' | 'help' | 'errors' | 'changes';
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

export interface Folder {
  id: FolderId;
  name: string;
  labels: FsDirentLabel[] | undefined;
  type: 'folder';
  isExpanded?: boolean;
  children?: FsDirent[] | undefined;
}

export interface Article {
  id: ArticleId;
  name: string;
  type: 'article';
  description?: string;
  isExpanded?: boolean;
  children?: FsDirent[];
}

export interface Language {
  id: LanguageId;
  name: string;
  type: 'language';
  description?: string;
}

export interface Service {
  id: ServiceId;
  name: string;
  type: 'service';
  isExpanded?: boolean;
  isReference?: boolean;
  children?: FsDirent[];
}

export interface Dialob {
  id: DialobId;
  name: string;
  type: 'dialob';
}

export interface Flow {
  id: FlowId;
  name: string;
  type: 'flow';
  isReference?: boolean;
}

export interface Link {
  id: LinkId;
  name: string;
  type: 'link';
  description?: string;
}

export interface Printout {
  id: PrintoutId;
  name: string;
  type: 'printout';
  isExpanded?: boolean;
  children?: FsDirent[];
}

export interface Image {
  id: ImageId;
  name: string;
  type: 'image';
  description?: string;
}

export interface Template {
  id: TemplateId;
  name: string;
  type: 'template';
  description?: string;
}

export interface FsDirentContextMenuData {
  dirent: FsDirent;
  anchorPosition: { top: number; left: number };
}