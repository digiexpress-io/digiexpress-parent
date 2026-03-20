import { FsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentCreateArticleProps } from './FsDirentCreateArticleProps';


export interface OwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
}

export const useOwnerState = (props: FsDirentCreateArticleProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode, parentFolder: props.parentFolder });
}