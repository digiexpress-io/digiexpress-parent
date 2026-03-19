import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentHistoryProps } from './FsDirentHistoryProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentHistoryProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
}