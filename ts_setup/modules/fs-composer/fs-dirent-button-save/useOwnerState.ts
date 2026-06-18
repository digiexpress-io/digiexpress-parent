import { FsDirentButtonSaveProps } from './FsDirentButtonSaveProps';
import { useFsTheme } from '../fs-theme';
import { FsDirentButtonOpenProps } from './FsDirentButtonOpenProps';

export interface OwnerState {
  isDarkMode: boolean;
  disabled: boolean;
}

export const useOwnerState = (props: FsDirentButtonOpenProps | FsDirentButtonSaveProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode, disabled: props.disabled ?? false };
};
