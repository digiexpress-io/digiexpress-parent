import * as React from 'react';
import { Box, IconButton, useTheme } from '@mui/material';
import type { SxProps, Theme } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import TodayIcon from '@mui/icons-material/Today';
import { useIntl } from 'react-intl';

import { CalendarInput, CalendarInputProvider, useCalendarInput } from '../calendar-input';
import { CalendarProvider } from '../calendar-interactive';

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
  /** Control input height similar to MUI TextField */
  size?: 'small' | 'medium';
  /** Force error visuals (in addition to internal invalid state) */
  error?: boolean;
  /** Visual style: keep "classic" as default to avoid regressions */
  variant?: 'classic' | 'mui-like';
  /** Extra Box sx for outer wrapper */
  sx?: SxProps<Theme>;
}

type FieldProps = {
  onClear: () => void;
  onOpen: () => void;
  size: 'small' | 'medium';
  error?: boolean;
  variant?: 'classic' | 'mui-like';
};

/** Internal field container that draws the border and holds the input + buttons */
const DateFieldContainer: React.FC<FieldProps> = ({ onClear, onOpen, size, error, variant = 'classic' }) => {
  const { machine } = useCalendarInput();
  const theme = useTheme();

  const isPartiallyFilled = machine.day !== '' || machine.month !== '' || machine.year !== '';
  const showError = (!!error) || (!machine.isValid && isPartiallyFilled);

  const height = size === 'small' ? 40 : 56;

  const classicStyles = {
    height,
    border: '1px solid #ccc',
    borderRadius: 4,
    px: 1,
    width: 'fit-content' as const,
    marginInline: 'auto',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    '&:focus-within': { borderColor: 'blue' },
  };

  const muiLikeStyles = {
    height,
    border: `1px solid ${showError ? theme.palette.error.main : 'rgba(0,0,0,0.23)'}`,
    borderRadius: theme.shape.borderRadius,
    px: 1,
    width: '100%',
    boxSizing: 'border-box' as const,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    '&:focus-within': {
      borderColor: showError ? theme.palette.error.main : theme.palette.primary.main,
      boxShadow: `0 0 0 2px ${theme.palette.action.focus}`,
    },
  };

  return (
    <Box sx={variant === 'classic' ? classicStyles : muiLikeStyles}>
      <CalendarInput className="calendar-input" />
      <Box display="flex" alignItems="center" ml={0.5}>
        <IconButton size="small" onClick={onClear} aria-label="Clear">
          <CloseIcon fontSize="small" />
        </IconButton>
        <IconButton size="small" onClick={onOpen} aria-label="Open calendar">
          <TodayIcon fontSize="small" />
        </IconButton>
      </Box>
    </Box>
  );
};

export const DatePicker: React.FC<DatePickerProps> = ({
  value,
  inline = false,
  onChange,
  fullWidth = false,
  size = 'medium',
  error,
  variant = 'classic',
  sx,
}) => {
  const { locale } = useIntl();
  const [open, setOpen] = React.useState(false);

  const handleClose = () => setOpen(false);
  const handleDateChange = (date: Date | null) => {
    setOpen(false);
    onChange(date);
  };
  const handleCalendarOpen = () => setOpen(true);

  return (
    <>
      {!open && (
        <CalendarInputProvider value={value} onChange={onChange} onCalendarOpen={handleCalendarOpen}>
          <Box width={fullWidth ? '100%' : (variant === 'classic' ? 'fit-content' : '100%')} sx={sx}>
            <DateFieldContainer
              onClear={() => handleDateChange(null)}
              onOpen={handleCalendarOpen}
              size={size}
              error={error}
              variant={variant}
            />
          </Box>
        </CalendarInputProvider>
      )}

      {open && (
        <CalendarProvider
          inline={inline}
          locale={locale}
          open={open}
          onClose={handleClose}
          value={value}
          onChange={handleDateChange}
        />
      )}
    </>
  );
};
