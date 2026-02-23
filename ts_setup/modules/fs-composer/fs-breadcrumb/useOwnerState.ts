import { FsNode, useFs } from "@dxs-ts/fs-api";
import { FsBreadcrumbProps } from "./FsBreadcrumbProps";


export interface OwnerState {
  isDarkMode: boolean;
  assetName: string | undefined; 
  assetPath: string | undefined;
  assetNode: FsNode | undefined;
  isError: boolean | undefined;
}

export function useOwnerState(_props: FsBreadcrumbProps): OwnerState {
  const { isDarkMode, activeTabPath, activeNode } = useFs();

  const pathParts = activeTabPath.split(' / ');
  const assetName = pathParts[pathParts.length - 1];
  const assetPath = pathParts.slice(0, -1).join(' / ');

  return {
    assetNode: activeNode,
    assetName,
    assetPath,
    isError: activeNode?.error,
    isDarkMode
  }
}