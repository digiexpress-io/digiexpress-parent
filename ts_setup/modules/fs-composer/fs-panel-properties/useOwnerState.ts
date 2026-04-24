import { Fs, useFsNav } from '@dxs-ts/fs-api';
import { FsPanelPropertiesProps } from './FsPanelPropertiesProps';


export interface OwnerState {
  isDarkMode: boolean;
  direntProps: Fs.DirentBase | undefined;
}

export const useOwnerState = (props: FsPanelPropertiesProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode, direntProps: props.dirent });
}
