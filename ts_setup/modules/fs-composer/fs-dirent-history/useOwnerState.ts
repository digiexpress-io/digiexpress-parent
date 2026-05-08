import { FsDirentHistoryProps } from './FsDirentHistoryProps';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentHistoryProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
}