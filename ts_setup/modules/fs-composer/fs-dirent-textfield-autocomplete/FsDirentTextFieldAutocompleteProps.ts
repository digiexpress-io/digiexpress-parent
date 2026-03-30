export interface FsDirentTextFieldAutocompleteProps {
  options: string[];
  value: string[];
  onChange: (value: string[]) => void;
  placeholder?: string;
}
