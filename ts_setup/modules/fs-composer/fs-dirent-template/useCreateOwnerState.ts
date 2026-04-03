import { Fs, useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentFolder: Fs.DirentBase | undefined;
  printoutServiceId: string;
}

export const useCreateOwnerState = (props: { parentFolder: Fs.DirentBase | undefined; pathToTopParent: string | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();

  const printoutServiceId = props.parentFolder?.type === 'printout' ? props.parentFolder.id : '';

  return ({ isDarkMode, parentFolder: props.parentFolder, printoutServiceId });
};
