import React from 'react';
import { Popover, type SxProps, type Theme } from '@mui/material';

import { YearPicker as YearGrid } from '../calendar-interactive/YearPicker';
import { useUtilityClasses, XuiYearPickerRoot } from './useUtilityClasses';
import { YearFieldContainer } from './YearFieldContainer';
import { YearInputProvider } from './YearInputProvider';


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
  const currentYear = value ?? new Date().getFullYear();

  const handleClose = () => setIsOpen(false);
  const handleOpen = () => setIsOpen(true);
  const handleYearSelect = (year: number) => {
    onChange(year);
    setIsOpen(false);
  };

  return (
    <>
      <YearInputProvider value={value} onChange={onChange} disabled={disabled} onOpen={handleOpen}>
        <XuiYearPickerRoot sx={{ ...sx }} ownerState={{ fullWidth }} className={classes.root} ref={anchorRef}>
          <YearFieldContainer
            size={size}
            onValidity={onValidity ?? (() => {})}
          />
        </XuiYearPickerRoot>
      </YearInputProvider>

      {disabled === true ? <></> : <Popover
        open={isOpen}
        onClose={handleClose}
        anchorEl={anchorRef.current}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
        transformOrigin={{ vertical: 'top', horizontal: 'left' }}
      >
        <YearGrid
          currentYear={currentYear}
          onYearSelect={handleYearSelect}
          onClose={handleClose}
        />
      </Popover>}
    </>
  );
};
