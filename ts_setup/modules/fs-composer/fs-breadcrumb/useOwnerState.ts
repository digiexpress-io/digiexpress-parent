import { FsDirent, useFsNav, useFsDirentProps } from "@dxs-ts/fs-api";
import { FsBreadcrumbProps } from "./FsBreadcrumbProps";


export interface OwnerState {
  isDarkMode: boolean;
  assetName: string | undefined;
  assetPath: string | undefined;
  assetDirent: FsDirent | undefined;
  isError: boolean | undefined;
}

export function useOwnerState(_props: FsBreadcrumbProps): OwnerState {
  const { isDarkMode, activeTabPath, activeDirent } = useFsNav();
  const { getDirentProps } = useFsDirentProps();

  const pathParts = activeTabPath.split(' / ');
  const assetName = pathParts[pathParts.length - 1];
  const assetPath = pathParts.slice(0, -1).join(' / ');

  const direntProps = getDirentProps(activeDirent?.id ?? '');
  const isError = direntProps.errors.length > 0;

  return {
    assetDirent: activeDirent,
    assetName,
    assetPath,
    isError,
    isDarkMode
  }
}