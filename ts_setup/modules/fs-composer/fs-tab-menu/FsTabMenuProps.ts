export interface FsTabMenuProps {
  anchorPosition: { top: number; left: number } | undefined;
  tabIndex: number | undefined;
  open: boolean;
  onClose: () => void;
}
