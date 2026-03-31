import { Fs } from "@dxs-ts/fs-api";


interface FsDirentMenuProps {
  dirent: Fs.DirentBase | undefined;
  anchorPosition: { top: number; left: number } | undefined;
  open: boolean;
  onClose: () => void;
  onExited?: () => void;
}

interface FsDirentMenuMainProps {
  dirent: Fs.DirentBase | undefined;
  openSubmenu: string | undefined;
  onSubmenuOpen: (submenuType: string) => void;
  onClose: () => void;
}

interface FsDirentMenuSubProps {
  dirent: Fs.DirentBase | undefined;
  openSubmenu: string | undefined;
}

interface DirentCommentsProps {
  dirent: Fs.DirentBase | undefined;
}

interface ItemHistoryEntry {
  user: string;
  change: string;
  date: string;
}


interface DirentReferencesProps {
  dirent?: Fs.DirentBase;
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

