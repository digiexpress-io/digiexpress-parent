import { Fs, useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentFolder: Fs.DirentBase | undefined;
}

export const useCreateOwnerState = (props: { parentFolder: Fs.DirentBase | undefined; pathToTopParent: string | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode, parentFolder: props.parentFolder });
};
