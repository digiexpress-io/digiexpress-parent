import { FsNode } from '@dxs-ts/fs-api';

export interface FsExplorerNodeProps {
  node: FsNode;
  level: number;
  parentPath?: string;
  onToggle: (nodeId: string) => void;
  onContextMenu: (event: React.MouseEvent, node: FsNode) => void;
  searchTerm: string;
}

export interface ExplorerNodeNameProps {
  node: FsNode;
  isDarkTheme: boolean;
  error: boolean;
  searchTerm: string;
}


