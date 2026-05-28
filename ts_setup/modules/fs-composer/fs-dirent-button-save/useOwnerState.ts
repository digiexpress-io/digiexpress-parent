import { FsDirentButtonSaveProps } from './FsDirentButtonSaveProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
  disabled: boolean;
}

export const useOwnerState = (props: FsDirentButtonSaveProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode, disabled: props.disabled ?? false };
};
