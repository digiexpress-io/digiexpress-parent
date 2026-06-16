import { useFsTheme } from '../fs-theme';


export interface CreateOwnerState {
  isDarkMode: boolean;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();

  return ({ isDarkMode });
};
