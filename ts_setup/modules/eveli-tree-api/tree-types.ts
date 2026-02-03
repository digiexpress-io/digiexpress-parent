// Mock data structure for EveliTree component
export interface TreeNode {
  id: string;
  name: string;
  description?: string;
  children?: TreeNode[];
  labels?: TreeNodeLabel[] | undefined;
  comments?: TreeNodeComment[] | undefined;
  isExpanded?: boolean;
  isReference?: boolean;
  isLocked?: boolean;
  type: TreeNodeType;
  configOptions?: ConfigOption[] | undefined;
}

export interface ConfigOption {
  devMode?: boolean | undefined;
  assignableMode?: boolean | undefined;
  disabledMode?: boolean | undefined;
  anonymousMode?: boolean | undefined;
}

export interface TreeNodeLabel {
  id: TreeNodeLabelId;
  value: string;
  nodeId: string;
}

export interface TreeNodeComment {
  comment: string;
  author: string;
  created: string;
}

export type TreeNodeType = 'folder' | 'article' | 'service' | 'dialob' | 'flow' | 'link' | 'language' | 'printout' | 'image' | 'template';

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
export type TreeNodeLabelId = string;

export interface Folder {
  id: FolderId;
  name: string;
  labels: TreeNodeLabel[] | undefined;
  type: 'folder';
  isExpanded?: boolean;
  children?: TreeNode[] | undefined;
}

export interface Article {
  id: ArticleId;
  name: string;
  type: 'article';
  description?: string;
  isExpanded?: boolean;
  children?: TreeNode[];
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
  children?: TreeNode[];
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
  children?: TreeNode[];
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

export interface ContextMenuData {
  node: TreeNode;
  anchorPosition: { top: number; left: number };
}