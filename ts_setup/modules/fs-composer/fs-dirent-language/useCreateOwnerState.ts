import { FsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent.Dirent | undefined;
}

export const useCreateOwnerState = (props: { parentFolder: FsDirent.Dirent | undefined; pathToTopParent: string | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode, parentFolder: props.parentFolder });
};
