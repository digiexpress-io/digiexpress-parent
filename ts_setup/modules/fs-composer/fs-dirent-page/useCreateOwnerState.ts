import { FsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
  pathToTopParent: string;
}

export const useCreateOwnerState = (props: { parentFolder: FsDirent | undefined; pathToTopParent: string | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();

  const pathToTopParent = props.pathToTopParent ?? '';

  return ({ isDarkMode, parentFolder: props.parentFolder, pathToTopParent });
};
