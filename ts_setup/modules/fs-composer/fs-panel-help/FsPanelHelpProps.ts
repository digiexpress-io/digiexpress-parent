import { Fs } from "@dxs-ts/fs-api";
import { Components } from "react-markdown";


export interface FsPanelHelpProps {
  dirent: Fs.DirentBase | undefined;
  remarkPlugins?: any[] | undefined;
  overrides?: Components;
}


