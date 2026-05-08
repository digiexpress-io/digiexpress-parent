import { FsPanelProps } from './FsPanelProps';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPanelProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode});
}