import { FsPanelErrorsProps } from './FsPanelErrorsProps';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelErrorsProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode});
}