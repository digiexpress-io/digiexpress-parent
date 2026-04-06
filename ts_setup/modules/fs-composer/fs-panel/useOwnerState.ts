import { useFsNav } from '@dxs-ts/fs-api';
import { FsPanelProps } from './FsPanelProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode});
}