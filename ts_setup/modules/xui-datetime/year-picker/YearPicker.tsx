import React from 'react';
import { Popover, type SxProps, type Theme } from '@mui/material';

import { YearPicker as YearGrid } from '../calendar-interactive/YearPicker';
import { useUtilityClasses, XuiYearPickerRoot } from './useUtilityClasses';
import { YearFieldContainer } from './YearFieldContainer';


const START_YEAR = 1925;
const END_YEAR = new Date().getFullYear() + 75;


export interface YearPickerProps {
  value: number | null;
  disabled?: boolean;
  fullWidth?: boolean;
  size?: 'small' | 'medium';
  sx?: SxProps<Theme>;
  onChange: (year: number | null) => void;
  onValidity?: (isError: boolean) => void;
}


export const YearPicker: React.FC<YearPickerProps> = ({ value, disabled, fullWidth = false, size = 'medium', sx, onChange, onValidity }) => {
  const classes = useUtilityClasses();
  const anchorRef = React.useRef<HTMLDivElement | null>(null);
  const [isOpen, setIsOpen] = React.useState(false);
  const [inputValue, setInputValue] = React.useState<string>(value !== null ? String(value) : '');
  const [isError, setIsError] = React.useState(false);

  const currentYear = value ?? new Date().getFullYear();

  React.useEffect(() => {
    setInputValue(value !== null ? String(value) : '');
  }, [value]);

  function handleInputChange(event: React.ChangeEvent<HTMLInputElement>) {
    const digits = event.target.value.replace(/\D/g, '').slice(0, 4);
    setInputValue(digits);
    if (digits.length === 4) {
      const year = parseInt(digits, 10);
      if (year >= START_YEAR && year <= END_YEAR) {
        setIsError(false);
        onValidity?.(false);
        onChange(year);
      } else {
        setIsError(true);
        onValidity?.(true);
        onChange(null);
      }
    } else {
      setIsError(false);
      onValidity?.(false);
      onChange(null);
    }
  }

  function handleClear() {
    setInputValue('');
    setIsError(false);
    onValidity?.(false);
    onChange(null);
  }

  function handleYearSelect(year: number) {
    onChange(year);
    setInputValue(String(year));
    setIsError(false);
    onValidity?.(false);
    setIsOpen(false);
  }

  return (
    <>
      <XuiYearPickerRoot sx={{ ...sx }} ownerState={{ fullWidth }} className={classes.root} ref={anchorRef}>
        <YearFieldContainer
          value={inputValue}
          isError={isError}
          disabled={disabled}
          size={size}
          onChange={handleInputChange}
          onClear={handleClear}
          onOpen={() => setIsOpen(true)}
        />
      </XuiYearPickerRoot>

      {disabled === true ? <></> : <Popover
        open={isOpen}
        onClose={() => setIsOpen(false)}
        anchorEl={anchorRef.current}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
        transformOrigin={{ vertical: 'top', horizontal: 'left' }}
      >
        <YearGrid
          currentYear={currentYear}
          onYearSelect={handleYearSelect}
          onClose={() => setIsOpen(false)}
        />
      </Popover>}
    </>
  );
};
