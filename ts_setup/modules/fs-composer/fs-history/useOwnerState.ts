import { useFsNav } from '@dxs-ts/fs-api';
import { FsHistoryProps } from './FsHistoryProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsHistoryProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode});
}