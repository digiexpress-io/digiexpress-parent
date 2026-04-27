import { Fs } from "@dxs-ts/fs-api";

export interface FsDirentMenuNewProps {
  dirent: Fs.DirentBase | undefined;
  onClose: () => void;
}
