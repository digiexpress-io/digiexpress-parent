import { FsDirent } from "@dxs-ts/fs-api";

export interface FsDirentMenuNewProps {
  dirent: FsDirent.Entry | undefined;
  onClose: () => void;
}
