import { FsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentCreateFolderProps } from './FsDirentCreateFolderProps';


export interface OwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
  locationPath: string;
}

function getFolderLocationPath(pathToTopParent: string | undefined, parentFolder: FsDirent | undefined): string {
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

export const useOwnerState = (props: FsDirentCreateFolderProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const locationPath = getFolderLocationPath(props.pathToTopParent, props.parentFolder);
  return { isDarkMode, parentFolder: props.parentFolder, locationPath };
}
