import { FsNode } from "@dxs-ts/fs-api";
import { Components } from "react-markdown";


export interface FsHelpProps {
  node: FsNode | undefined;
  remarkPlugins?: any[] | undefined;
  overrides?: Components;
}


