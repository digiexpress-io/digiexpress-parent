import { useFs } from '@dxs-ts/fs-api';
import { FsErrorsProps } from './FsErrorsProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsErrorsProps): OwnerState => {
  const { isDarkMode } = useFs();

  return ({ isDarkMode});
}