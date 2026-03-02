import { FsNode } from '@dxs-ts/fs-api';

export interface FsDirentProps {
  node: FsNode;
  level: number;
  parentPath?: string;
  onToggle: (nodeId: string) => void;
  onContextMenu: (event: React.MouseEvent, node: FsNode) => void;
  searchTerm: string;
}

export interface FsDirentNameProps {
  node: FsNode;
  isDarkTheme: boolean;
  error: boolean;
  searchTerm: string;
}


