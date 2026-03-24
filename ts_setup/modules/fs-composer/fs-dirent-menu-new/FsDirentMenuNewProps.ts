import { FsDirentEntry } from "@dxs-ts/fs-api";

export interface FsDirentMenuNewProps {
  dirent: FsDirentEntry | undefined;
  onClose: () => void;
}
