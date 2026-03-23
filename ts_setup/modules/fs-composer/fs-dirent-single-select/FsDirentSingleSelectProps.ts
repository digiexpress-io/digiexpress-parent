export interface FsDirentSingleSelectOption {
  value: string;
  label: string;
}

export interface FsDirentSingleSelectProps {
  options: FsDirentSingleSelectOption[];
  value: string;
  onChange: (value: string) => void;
}
