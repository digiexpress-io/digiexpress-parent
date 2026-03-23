export interface FsDirentMultiSelectOption {
  value: string;
  label: string;
}

export interface FsDirentMultiSelectProps {
  options: FsDirentMultiSelectOption[];
  value: string[];
  onChange: (value: string[]) => void;
}
