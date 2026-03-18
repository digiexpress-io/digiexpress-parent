// Mock data structure for Fs component
export interface FsNode {
  id: string;
  name: string;
  description?: string;
  children?: FsNode[];
  labels?: FsNodeLabel[] | undefined;
  comments: FsNodeComment[] | undefined;
  expanded: boolean;
  reference: boolean;
  locked: boolean;
  type: FsNodeType;
  configOptions?: ConfigOption[] | undefined;
  changes: FsNodeChange[];
  permissions: Permission[];
  errors: FsNodeError[] | undefined;
}

export interface ConfigOption {
  devMode?: boolean | undefined;
  assignableMode?: boolean | undefined;
  disabledMode?: boolean | undefined;
  anonymousMode?: boolean | undefined;
}

export interface User {
  userName: string;
  email: string;
  permissions?: PermissionType[];
}

export interface FsNodeError {
  code: string;
  severity: ErrorSeverityType;
  message: string;
}

export interface FsNodeChange {
  changeType: FsNodeChangeType;
  changeDate: string;
  changedBy: User;
}

export interface FsNodeLabel {
  id: FsNodeLabelId;
  value: string;
  nodeId: string;
}

export interface Permission {
  name: string;
  types: PermissionType[];
}

export interface FsNodeComment {
  comment: string;
  author: string;
  created: string;
}

export type FsNodeType = 'folder' | 'article' | 'service' | 'dialob' | 'flow' | 'link' | 'language' | 'printout' | 'image' | 'template';
export type FsNodeSecondaryView = 'references' | 'properties' | 'configuration' | 'debug' | 'preview' | 'history' | 'help' | 'errors' | 'changes';
export type FsNodeChangeType = 'update' | 'create' | 'delete';
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
export type FsNodeLabelId = string;

export interface Folder {
  id: FolderId;
  name: string;
  labels: FsNodeLabel[] | undefined;
  type: 'folder';
  isExpanded?: boolean;
  children?: FsNode[] | undefined;
}

export interface Article {
  id: ArticleId;
  name: string;
  type: 'article';
  description?: string;
  isExpanded?: boolean;
  children?: FsNode[];
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
  children?: FsNode[];
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
  children?: FsNode[];
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

export interface FsContextMenuData {
  node: FsNode;
  anchorPosition: { top: number; left: number };
}