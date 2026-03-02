import { useFs } from '@dxs-ts/fs-api';
import { FsMainContentProps } from './FsMainContentProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsMainContentProps): OwnerState => {
  const { isDarkMode } = useFs();

  return ({ isDarkMode});
}