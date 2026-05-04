import { Fs, FsTab } from '@dxs-ts/fs-api';


export interface FsDirentWorkflowCreateProps {
  parentFolder: Fs.DirentBase | undefined;
  pathToTopParent: string | undefined;
}

export interface FsDirentWorkflowUpdateProps {
  direntId: string;
}

export interface FsDirentWorkflowProps {
  tab: FsTab;
}
