import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { FsPanelPropertiesProps } from './FsPanelPropertiesProps';


export interface OwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
}

export const useOwnerState = (props: FsPanelPropertiesProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent } = useFsDirent();

  return ({ isDarkMode, dirent: props.dirent ? getDirent(props.dirent.id) : undefined });
}