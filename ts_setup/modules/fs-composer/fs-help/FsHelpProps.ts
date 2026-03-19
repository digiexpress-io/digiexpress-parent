import { FsDirent } from "@dxs-ts/fs-api";
import { Components } from "react-markdown";


export interface FsHelpProps {
  dirent: FsDirent | undefined;
  remarkPlugins?: any[] | undefined;
  overrides?: Components;
}


