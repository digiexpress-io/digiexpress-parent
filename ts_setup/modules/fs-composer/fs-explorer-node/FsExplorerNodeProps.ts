import { FsNode, FsNodeType } from '@dxs-ts/fs-api';

export interface FsExplorerNodeProps {
  node: FsNode;
  level: number;
  parentPath?: string;
  onToggle: (nodeId: string) => void;
  onContextMenu: (event: React.MouseEvent, node: FsNode) => void;
  searchTerm: string;
}

export interface ExplorerNodeNameProps {
  nodeType: FsNodeType;
  nodeName: string;
  description?: string;
  isDarkTheme: boolean;
  error: boolean;
  searchTerm: string;
}