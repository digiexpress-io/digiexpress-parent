export interface FsDirentTextFieldProps {
  placeholder?: string;
  value?: string;
  disabled?: boolean;
  multiline?: boolean;
  minRows?: number;
  maxRows?: number;
  required?: boolean;
  onChange?: (value: string) => void;
}
