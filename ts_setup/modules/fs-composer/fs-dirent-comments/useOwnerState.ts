import { FsDirentCommentsProps } from './FsDirentCommentsProps';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentCommentsProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
}