import { FsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentCreatePhoneProps } from './FsDirentCreatePhoneProps';

const MOCK_LOCALES = ['en', 'fi', 'sv'];

export interface OwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
  locales: string[];
}

export const useOwnerState = (props: FsDirentCreatePhoneProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  return ({ isDarkMode, parentFolder: props.parentFolder, locales: MOCK_LOCALES });
}
