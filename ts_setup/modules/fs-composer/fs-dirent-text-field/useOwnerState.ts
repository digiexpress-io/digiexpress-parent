import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentTextFieldProps } from './FsDirentTextFieldProps';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentTextFieldProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return { isDarkMode };
};
