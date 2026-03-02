import { FsNode } from "@dxs-ts/fs-api";


interface FsNodeMenuProps {
  node: FsNode | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

interface FsNodeMenuMainProps {
  node: FsNode | undefined;
  openSubmenu: string | undefined;
  onSubmenuOpen: (submenuType: string) => void;
  onClose: () => void;
}

interface FsNodeMenuSubProps {
  node: FsNode | undefined;
  openSubmenu: string | undefined;
}

interface NodeCommentsProps {
  node: FsNode | undefined;
}

interface ItemHistoryEntry {
  user: string;
  change: string;
  date: string;
}


interface NodeReferencessProps {
  node?: FsNode;
}


/*
interface OwnerState {

  active: {

  }

  navigation: {
    toEdit();
    toHistory();
  },
  persistence: {
    save()
    copy()
  } 
*/

