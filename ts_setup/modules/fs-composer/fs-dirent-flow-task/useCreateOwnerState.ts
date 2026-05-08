import { Fs } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface CreateOwnerState {
  isDarkMode: boolean;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
};
