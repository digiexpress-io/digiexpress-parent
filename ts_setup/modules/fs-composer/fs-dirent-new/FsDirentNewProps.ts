import { FsDirent } from "@dxs-ts/fs-api";

export interface FsDirentNewProps {
  dirent: FsDirent | undefined;
  onClose: () => void;
}
