import { Fs } from "@dxs-ts/fs-api";

export interface FsDirentMenuNewProps {
  dirent: Fs.DirentAsset | undefined;
  onClose: () => void;
}
