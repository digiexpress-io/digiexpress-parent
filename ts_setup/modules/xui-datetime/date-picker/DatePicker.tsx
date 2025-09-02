import React from 'react';
import { Box, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import TodayIcon from '@mui/icons-material/Today';
import { useIntl } from 'react-intl';

import { CalendarInput, CalendarInputProvider, useCalendarInput } from '../calendar-input';
import { CalendarProvider } from '../calendar-interactive';

const DateFieldContainer: React.FC<{ onClear: () => void; onOpen: () => void }> = ({
  onClear,
  onOpen,
}) => {
  const { machine } = useCalendarInput();

  const showError = !machine.isValid && machine.day !== '' && machine.month !== '' && machine.year !== '';

  return (
    <Box
      display="flex"
      alignItems="center"
      justifyContent="space-between"
      sx={{
        border: `1px solid ${showError ? 'red' : '#ccc'}`,
        borderRadius: 2,
        padding: '2px 6px',
        width: 'fit-content',
        '&:focus-within': {
          borderColor: 'blue',
          boxShadow: '0 0 0 2px rgba(25, 118, 210, 0.2)',
        },
      }}
      
    >
      <CalendarInput className="calendar-input" />

      <Box display="flex" alignItems="center" ml={0.5}>
        <IconButton size="small" onClick={onClear}>
          <CloseIcon fontSize="small" />
        </IconButton>
        <IconButton size="small" onClick={onOpen}>
          <TodayIcon fontSize="small" />
        </IconButton>
      </Box>
    </Box>
  );
};

export interface DatePickerProps {
  value: Date | null;
  inline: boolean;
  onChange: (newDate: Date | null) => void;
}

export const DatePicker: React.FC<DatePickerProps> = ({ value, inline, onChange }) => {
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
          <Box display="flex" justifyContent="center" width="100%" mt={1}>
            <DateFieldContainer
              onClear={() => handleDateChange(null)}
              onOpen={handleCalendarOpen}
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
