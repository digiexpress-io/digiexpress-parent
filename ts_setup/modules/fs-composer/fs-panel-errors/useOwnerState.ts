import { useFsNav } from '@dxs-ts/fs-api';
import { FsPanelErrorsProps } from './FsPanelErrorsProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelErrorsProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode});
}