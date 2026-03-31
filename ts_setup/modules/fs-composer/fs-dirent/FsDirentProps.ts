import { Fs } from '@dxs-ts/fs-api';

export interface FsDirentProps {
  dirent: Fs.Dirent;
  level: number;
  parentPath?: string;
  onToggle: (direntId: string) => void;
  onContextMenu: (event: React.MouseEvent, dirent: Fs.Dirent) => void;
  searchTerm: string;
}
