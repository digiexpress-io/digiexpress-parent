import { useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
};
