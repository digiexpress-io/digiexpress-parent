import { Fs } from "@dxs-ts/fs-api";

export interface FsDirentMenuNewProps {
  dirent: Fs.Dirent | undefined;
  onClose: () => void;
}
