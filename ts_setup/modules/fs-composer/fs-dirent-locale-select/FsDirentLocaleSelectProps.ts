export interface FsDirentLocaleSelectProps {
  value: string;
  onChange?: (code: string) => void;
  disabled?: boolean;
  allowedCodes?: string[];
  placeholder?: string;
}
