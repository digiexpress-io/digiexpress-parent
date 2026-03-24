import { FsDirentEntry } from "@dxs-ts/fs-api";
import { Components } from "react-markdown";


export interface FsHelpProps {
  dirent: FsDirentEntry | undefined;
  remarkPlugins?: any[] | undefined;
  overrides?: Components;
}


