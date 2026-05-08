import { useFsNav } from '@dxs-ts/fs-nav';


export interface CreateOwnerState {
  isDarkMode: boolean;
  locales: string[];
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode, locales: [] });
};
