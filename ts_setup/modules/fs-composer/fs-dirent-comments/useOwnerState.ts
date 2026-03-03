import { useFs } from '@dxs-ts/fs-api';
import { FsDirentCommentsProps } from './FsDirentCommentsProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentCommentsProps): OwnerState => {
  const { isDarkMode } = useFs();

  return ({ isDarkMode });
}