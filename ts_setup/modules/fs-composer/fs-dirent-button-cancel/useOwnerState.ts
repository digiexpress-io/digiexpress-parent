import { FsDirentButtonCancelProps } from './FsDirentButtonCancelProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
  disabled: boolean;
}

export const useOwnerState = (props: FsDirentButtonCancelProps): OwnerState => {
  const { isDarkMode } = useFsTheme();
  return { isDarkMode, disabled: props.disabled ?? false };
};
