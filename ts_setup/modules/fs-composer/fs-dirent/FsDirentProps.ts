import { FsDirent } from '@dxs-ts/fs-api';

export interface FsDirentProps {
  dirent: FsDirent.Dirent;
  level: number;
  parentPath?: string;
  onToggle: (direntId: string) => void;
  onContextMenu: (event: React.MouseEvent, dirent: FsDirent.Dirent) => void;
  searchTerm: string;
}
