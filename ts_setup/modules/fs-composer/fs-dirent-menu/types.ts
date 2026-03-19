import { FsDirent } from "@dxs-ts/fs-api";


interface FsDirentMenuProps {
  dirent: FsDirent | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

interface FsDirentMenuMainProps {
  dirent: FsDirent | undefined;
  openSubmenu: string | undefined;
  onSubmenuOpen: (submenuType: string) => void;
  onClose: () => void;
}

interface FsDirentMenuSubProps {
  dirent: FsDirent | undefined;
  openSubmenu: string | undefined;
}

interface DirentCommentsProps {
  dirent: FsDirent | undefined;
}

interface ItemHistoryEntry {
  user: string;
  change: string;
  date: string;
}


interface DirentReferencesProps {
  dirent?: FsDirent;
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

