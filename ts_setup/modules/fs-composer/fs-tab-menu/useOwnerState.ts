import { useFsNav } from '@dxs-ts/fs-nav';
import { FsTabMenuProps } from './FsTabMenuProps';

export interface OwnerState {
  onCloseAll: () => void;
}

export function useOwnerState(props: FsTabMenuProps): OwnerState {
  const { closeAllTabs } = useFsNav();

  function onCloseAll() {
    closeAllTabs();
    props.onClose();
  }

  return {
    onCloseAll,
  };
}
