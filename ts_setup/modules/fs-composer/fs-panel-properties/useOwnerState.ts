import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsPanelPropertiesProps } from './FsPanelPropertiesProps';


export interface OwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
}

export const useOwnerState = (props: FsPanelPropertiesProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();

  return ({ isDarkMode, dirent: props.dirent ? getDirent(props.dirent.id) : undefined });
}