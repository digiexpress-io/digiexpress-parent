import { Fs } from "@dxs-ts/fs-api";

export interface FsDirentMenuNewProps {
  dirent: Fs.Entry | undefined;
  onClose: () => void;
}
