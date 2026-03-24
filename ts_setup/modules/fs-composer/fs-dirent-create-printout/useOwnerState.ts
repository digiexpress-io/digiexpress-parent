import { FsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentCreatePrintoutProps } from './FsDirentCreatePrintoutProps';

const MOCK_LOCALES = ['en', 'fi', 'sv'];

export interface OwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
  locales: string[];
}

export const useOwnerState = (props: FsDirentCreatePrintoutProps): OwnerState => {
  const { isDarkMode } = useFsNav();

  return ({ isDarkMode, parentFolder: props.parentFolder, locales: MOCK_LOCALES });
};
