export interface FsTabMenuProps {
  anchorPosition: { top: number; left: number } | undefined;
  tabId: string | undefined;
  open: boolean;
  onClose: () => void;
}
