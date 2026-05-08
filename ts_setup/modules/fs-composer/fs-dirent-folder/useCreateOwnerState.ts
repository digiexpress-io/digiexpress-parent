import { Fs } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


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
  const { isDarkMode } = useFsTheme();
  const locationPath = getLocationPath(props.parentFolder);
  return { isDarkMode, locationPath };
};
