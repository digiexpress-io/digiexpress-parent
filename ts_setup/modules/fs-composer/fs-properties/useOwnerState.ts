import { useFs } from '@dxs-ts/fs-api';
import { FsPropertiesProps } from './FsPropertiesProps';


export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsPropertiesProps): OwnerState => {
  const { isDarkMode } = useFs();

  return ({ isDarkMode});
}