import { FsDirentPermissionsProps } from './FsDirentPermissionsProps';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentPermissionsProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
}