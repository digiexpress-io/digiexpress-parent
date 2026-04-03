import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentFolder: Fs.DirentBase | undefined;
  locales: string[];
}

export const useCreateOwnerState = (props: { parentFolder: Fs.DirentBase | undefined; pathToTopParent: string | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { selectOptions } = useFsDirent();
  const locales = selectOptions.languages;

  return ({ isDarkMode, parentFolder: props.parentFolder, locales });
};
