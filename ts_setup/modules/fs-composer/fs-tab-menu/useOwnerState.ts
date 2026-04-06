import { useFsNav } from '@dxs-ts/fs-api';
import { FsTabMenuProps } from './FsTabMenuProps';

export interface OwnerState {
  isDarkMode: boolean;
  onCloseAll: () => void;
}

export function useOwnerState(props: FsTabMenuProps): OwnerState {
  const { isDarkMode, closeAllTabs } = useFsNav();

  function onCloseAll() {
    closeAllTabs();
    props.onClose();
  }

  return {
    isDarkMode,
    onCloseAll,
  };
}
