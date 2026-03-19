import { useFsNav } from "@dxs-ts/fs-api";
import { FsHelpProps } from "./FsHelpProps";



export interface OwnerState {
    isDarkMode: boolean;
}

export const useOwnerState = (_props: FsHelpProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode });
}