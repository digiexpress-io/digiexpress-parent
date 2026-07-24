export interface FsDirentSelectGroupedItem {
  id: string;
  label: string;
  desc?: string;
}

export interface FsDirentSelectGroup {
  localeId: string;
  localeLabel: string;
  items: FsDirentSelectGroupedItem[];
}

export interface FsDirentSelectGroupedProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  value: string[];
  onChange: (value: string[]) => void;
  groups: FsDirentSelectGroup[];
}
