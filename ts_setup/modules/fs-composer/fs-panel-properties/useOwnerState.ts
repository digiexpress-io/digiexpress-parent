import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';
import { FsPanelPropertiesProps } from './FsPanelPropertiesProps';


export interface OwnerState {
  dirent: Fs.DirentBase | undefined;
}

export const useOwnerState = (props: FsPanelPropertiesProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();

  return ({ dirent: props.dirent ? getDirent(props.dirent.id) : undefined });
}