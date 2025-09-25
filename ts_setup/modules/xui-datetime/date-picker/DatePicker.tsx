import React from 'react';
import type { SxProps, Theme } from '@mui/material';
import { useIntl } from 'react-intl';

import { CalendarInputProvider } from '../calendar-input';
import { CalendarProvider, CalendarProviderProps } from '../calendar-interactive';
import { useUtilityClasses, XuiDateFieldRoot } from './useUtilityClasses';
import { DateFieldContainer } from './DateFieldContainer';

/**
 * DatePicker
 *  - Default visual behaviour remains the existing one ("classic")
 *  - Opt-in "mui-like" variant aligns visuals with MUI OutlinedInput
 */
export interface DatePickerProps {
  value: Date | null;
  inline?: boolean;
  onChange: (newDate: Date | null) => void;
  /** Stretch to container width (used in "mui-like" UIs) */
  fullWidth?: boolean;

  popover?: boolean;

  /** Control input height similar to MUI TextField */
  size?: 'small' | 'medium';
  /** Force error visuals (in addition to internal invalid state) */
  error?: boolean;
  /** Visual style: keep "classic" as default to avoid regressions */
  variant?: 'classic' | 'mui-like';
  /** Extra Box sx for outer wrapper */
  sx?: SxProps<Theme>;
}


export const DatePicker: React.FC<DatePickerProps> = ({
  value,
  error,
  sx,
  onChange,
  popover = false,
  inline = false,
  fullWidth = false,
  size = 'medium',
  variant = 'classic',
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
  
  const isFieldEnabled = !isPickerOpen || popover;


  return (
    <>
      {isFieldEnabled && (
        <CalendarInputProvider value={value} onChange={onChange} onCalendarOpen={handleCalendarOpen}>
          <XuiDateFieldRoot sx={{ ...sx }} ownerState={{ variant, fullWidth }} className={classes.root}>
            <DateFieldContainer
              onClear={() => handleDateChange(null)}
              onOpen={handleCalendarOpen}
              size={size}
              error={error}
              variant={variant}
            />
          </XuiDateFieldRoot>
        </CalendarInputProvider>
      )}

      <PickerFactory open={isPickerOpen} popover={popover}>
        <CalendarProvider
          inline={inline}
          locale={locale}
          open={isPickerOpen}
          onClose={handleClose}
          value={value}
          onChange={handleDateChange} />
      </PickerFactory>
    </>
  );
}


const PickerFactory: React.FC<{ children: React.ReactNode, open: boolean, popover: boolean }> = ({
  children, popover, open
}) => {
  if (!open) {
    return (<></>)
  }

  if (popover) {
    return (<PickerPopover children={children} />)
  }

  return (<>{children}</>);
}



const PickerPopover: React.FC<{ children: React.ReactNode }> = ({
  children
}) => {

  return (<>fdf</>)
}

