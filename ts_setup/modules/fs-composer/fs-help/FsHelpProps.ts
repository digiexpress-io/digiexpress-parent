import { Fs } from "@dxs-ts/fs-api";
import { Components } from "react-markdown";


export interface FsHelpProps {
  dirent: Fs.DirentAsset | undefined;
  remarkPlugins?: any[] | undefined;
  overrides?: Components;
}


