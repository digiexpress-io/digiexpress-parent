import { useFs } from '@dxs-ts/fs-api';
import { FsDirentPermissionsProps } from './FsDirentPermissionsProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentPermissionsProps): OwnerState => {
  const { isDarkMode } = useFs();

  return ({ isDarkMode });
}