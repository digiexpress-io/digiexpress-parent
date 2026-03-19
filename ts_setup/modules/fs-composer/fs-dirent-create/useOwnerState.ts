import { useFsNav } from '@dxs-ts/fs-api';
import { FsDirentCreateProps } from './FsDirentCreateProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentCreateProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
}