import { FsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentCreateDialobProps } from './FsDirentCreateDialobProps';


export interface OwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
}

export const useOwnerState = (props: FsDirentCreateDialobProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode, parentFolder: props.parentFolder });
}
