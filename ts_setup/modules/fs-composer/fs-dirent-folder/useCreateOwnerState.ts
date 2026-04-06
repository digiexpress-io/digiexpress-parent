import { Fs, useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
  locationPath: string;
}

function getFolderLocationPath(pathToTopParent: string | undefined, parentFolder: Fs.DirentBase | undefined): string {
  if (!parentFolder) {
    return '';
  }
  if (parentFolder.type === 'folder') {
    return pathToTopParent || parentFolder.name;
  }
  // parentFolder is a non-folder dirent — strip its name from the path to get the containing folder
  if (!pathToTopParent) {
    return '';
  }
  const segments = pathToTopParent.split(' / ');
  return segments.slice(0, -1).join(' / ');
}

export const useCreateOwnerState = (props: { parentFolder: Fs.DirentBase | undefined; pathToTopParent: string | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();
  const locationPath = getFolderLocationPath(props.pathToTopParent, props.parentFolder);
  return { isDarkMode, locationPath };
};
