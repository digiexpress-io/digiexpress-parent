import { Fs } from "@dxs-ts/fs-api";


interface FsDirentMenuProps {
  dirent: Fs.Dirent | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

interface FsDirentMenuMainProps {
  dirent: Fs.Dirent | undefined;
  openSubmenu: string | undefined;
  onSubmenuOpen: (submenuType: string) => void;
  onClose: () => void;
}

interface FsDirentMenuSubProps {
  dirent: Fs.Dirent | undefined;
  openSubmenu: string | undefined;
}

interface DirentCommentsProps {
  dirent: Fs.Dirent | undefined;
}

interface ItemHistoryEntry {
  user: string;
  change: string;
  date: string;
}


interface DirentReferencesProps {
  dirent?: Fs.Dirent;
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

