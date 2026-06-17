export interface FsDirentSelectSingleOption {
  value: string;
  label: string;
}

export interface FsDirentSelectSingleProps {
  options: FsDirentSelectSingleOption[];
  value: string;
  onChange: (value: string) => void;
  allowNone?: boolean;
  required?: boolean;
}
