import { useFsNav } from '@dxs-ts/fs-nav';
import { useFsTheme } from '../fs-theme';
import { FsTabMenuProps } from './FsTabMenuProps';

export interface OwnerState {
  isDarkMode: boolean;
  onCloseAll: () => void;
}

export function useOwnerState(props: FsTabMenuProps): OwnerState {
  const { isDarkMode } = useFsTheme();
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
