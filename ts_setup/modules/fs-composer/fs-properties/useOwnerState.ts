import { Fs, useFsNav } from '@dxs-ts/fs-api';
import { FsPropertiesProps } from './FsPropertiesProps';


export interface OwnerState {
  isDarkMode: boolean;
  direntProps: Fs.DirentAsset | undefined;
}

export const useOwnerState = (props: FsPropertiesProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode, direntProps: props.dirent });
}
