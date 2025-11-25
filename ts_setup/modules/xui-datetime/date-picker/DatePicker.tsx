import React from 'react';
import { Popover, type SxProps, type Theme } from '@mui/material';
import { useIntl } from 'react-intl';

import { CalendarInputProvider } from '../calendar-input';
import { CalendarProvider } from '../calendar-interactive';
import { useUtilityClasses, XuiDateFieldRoot } from './useUtilityClasses';
import { DateFieldContainer } from './DateFieldContainer';

/**
 * DatePicker
 *  - Default visual behaviour remains the existing one ("classic")
 *  - Opt-in "mui-like" variant aligns visuals with MUI OutlinedInput
 */
export interface DatePickerProps {
  value: Date | null;
  disabled?: boolean;
  onChange: (newDate: Date | null) => void;
  /** Stretch to container width (used in "mui-like" UIs) */
  fullWidth?: boolean;
  /** Control input height similar to MUI TextField */
  size?: 'small' | 'medium';
  /** Extra Box sx for outer wrapper */
  sx?: SxProps<Theme>;
  onValidity?: (isError: boolean) => void;
}

export const DatePicker: React.FC<DatePickerProps> = ({
  value,
  sx,
  disabled,
  onChange,
  onValidity,
  fullWidth = false,
  size = 'medium',

}) => {
  const { locale } = useIntl();
  const [isPickerOpen, setOpen] = React.useState(false);

  const handleClose = () => setOpen(false);
  const handleDateChange = (date: Date | null) => {
    setOpen(false);
    onChange(date);
  };
  const handleCalendarOpen = () => setOpen(true);
  const classes = useUtilityClasses();

  const anchorRef = React.useRef<HTMLDivElement | null>(null);

  return (
    <>
      <CalendarInputProvider value={value} onChange={onChange} onCalendarOpen={handleCalendarOpen} disabled={disabled}>
        <XuiDateFieldRoot sx={{ ...sx }} ownerState={{ fullWidth }} className={classes.root} ref={anchorRef}>
          <DateFieldContainer 
            onClear={() => handleDateChange(null)}
            onOpen={handleCalendarOpen}
            size={size}
            onValidity={onValidity ?? (() => { })}
          />
        </XuiDateFieldRoot>
      </CalendarInputProvider>

      {disabled === true ? <></> : <Popover
        open={isPickerOpen}
        onClose={handleClose}
        anchorEl={anchorRef.current}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'left',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'left',
        }}
      >
        <CalendarProvider
          locale={locale}
          open={isPickerOpen}
          onClose={handleClose}
          value={value}
          onChange={handleDateChange} />
      </Popover>}
    </>
  );
}


