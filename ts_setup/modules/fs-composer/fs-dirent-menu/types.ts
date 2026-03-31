import { FsDirent } from "@dxs-ts/fs-api";


interface FsDirentMenuProps {
  dirent: FsDirent.Dirent | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

interface FsDirentMenuMainProps {
  dirent: FsDirent.Dirent | undefined;
  openSubmenu: string | undefined;
  onSubmenuOpen: (submenuType: string) => void;
  onClose: () => void;
}

interface FsDirentMenuSubProps {
  dirent: FsDirent.Dirent | undefined;
  openSubmenu: string | undefined;
}

interface DirentCommentsProps {
  dirent: FsDirent.Dirent | undefined;
}

interface ItemHistoryEntry {
  user: string;
  change: string;
  date: string;
}


interface DirentReferencesProps {
  dirent?: FsDirent.Dirent;
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

