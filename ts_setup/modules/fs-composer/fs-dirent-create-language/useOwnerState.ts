import { FsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentCreateLanguageProps } from './FsDirentCreateLanguageProps';


export interface OwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
}

export const useOwnerState = (props: FsDirentCreateLanguageProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode, parentFolder: props.parentFolder });
};
