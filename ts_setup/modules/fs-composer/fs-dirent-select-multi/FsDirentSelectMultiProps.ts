export interface FsDirentSelectMultiOption {
  value: string;
  label: string;
}

export interface FsDirentSelectMultiProps {
  options: FsDirentSelectMultiOption[];
  value: string[];
  onChange: (value: string[]) => void;
}
