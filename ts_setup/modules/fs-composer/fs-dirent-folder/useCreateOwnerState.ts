import { Fs } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface CreateOwnerState {
  isDarkMode: boolean;
  locationPath: string;
}

function getLocationPath(parentFolder: Fs.DirentBase | undefined): string {
  if (!parentFolder) {
    return '';
  }
  if (parentFolder.type !== 'FOLDER') {
    return parentFolder.fullPath.split('/').slice(0, -1).join('/');
  }
  return parentFolder.fullPath;
}

export const useCreateOwnerState = (props: { parentFolder: Fs.DirentBase | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();
  const locationPath = getLocationPath(props.parentFolder);
  return { isDarkMode, locationPath };
};
