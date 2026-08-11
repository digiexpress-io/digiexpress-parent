import { useFsNav } from '@dxs-ts/fs-nav';
import { FsTabMenuProps } from './FsTabMenuProps';

export interface OwnerState {
  onClose: () => void;
  onCloseToTheRight: () => void;
  onCloseOthers: () => void;
  onCloseAll: () => void;
}

export function useOwnerState(props: FsTabMenuProps): OwnerState {
  const { closeTab, closeAllTabs, closeTabsToTheRight, closeOtherTabs } = useFsNav();

  function onClose() {
    if (props.tabId !== undefined) {
      closeTab(props.tabId);
    }
    props.onClose();
  }

  function onCloseToTheRight() {
    if (props.tabId !== undefined) {
      closeTabsToTheRight(props.tabId);
    }
    props.onClose();
  }

  function onCloseOthers() {
    if (props.tabId !== undefined) {
      closeOtherTabs(props.tabId);
    }
    props.onClose();
  }

  function onCloseAll() {
    closeAllTabs();
    props.onClose();
  }

  return {
    onClose,
    onCloseToTheRight,
    onCloseOthers,
    onCloseAll,
  };
}
