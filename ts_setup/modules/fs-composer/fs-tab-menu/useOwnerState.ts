import { useFsNav } from '@dxs-ts/fs-nav';
import { FsTabMenuProps } from './FsTabMenuProps';

export interface OwnerState {
  isDarkMode: boolean;
  onCloseAll: () => void;
}

export function useOwnerState(props: FsTabMenuProps): OwnerState {
  const { isDarkMode } = useFsNav();
  const { closeAllTabs } = useFsNav();

  function onCloseAll() {
    closeAllTabs();
    props.onClose();
  }

  return {
    isDarkMode,
    onCloseAll,
  };
}
