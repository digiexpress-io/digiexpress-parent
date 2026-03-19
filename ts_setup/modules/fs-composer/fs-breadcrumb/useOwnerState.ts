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

  const direntProps = activeDirent ? getDirentProps(activeDirent.id) : undefined;
  const isError = direntProps?.errors && direntProps.errors.length > 0 ? true : false;

  return {
    assetDirent: activeDirent,
    assetName,
    assetPath,
    isError,
    isDarkMode
  }
}