import { Fs } from '@dxs-ts/fs-api';

export interface FsDirentProps {
  dirent: Fs.DirentBase;
  level: number;
  setExpanded: (id: string) => boolean;
  activeDirentId: string | undefined;
  openAsset: (asset: Fs.DirentBase) => void;
  onToggle: (direntId: string) => void;
  onContextMenu: (event: React.MouseEvent, dirent: Fs.DirentBase) => void;
  searchTerm: string;
}
