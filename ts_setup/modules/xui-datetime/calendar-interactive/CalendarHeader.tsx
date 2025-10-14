import React from 'react';
import { Box, IconButton, useTheme } from '@mui/material';
import { KeyboardArrowLeft as KeyboardArrowLeftIcon } from '@mui/icons-material';
import { KeyboardArrowRight as KeyboardArrowRightIcon } from '@mui/icons-material';
import { ArrowDropDown as ArrowDropDownIcon } from '@mui/icons-material';
import { ArrowDropUp as ArrowDropUpIcon } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';

export interface CalendarHeaderProps {
  currentDate: Date;
  onPreviousMonth: () => void;
  onNextMonth: () => void;
  onYearClick: () => void; // toggle year picker
  showYearPicker?: boolean;
}

export const CalendarHeader: React.FC<CalendarHeaderProps> = ({
  currentDate,
  onPreviousMonth,
  onNextMonth,
  onYearClick,
  showYearPicker = false,
}) => {

  const theme = useTheme();
  return (
    <Box>
      <Box display="flex" alignItems="center" justifyContent="space-between" px={1}>

        {/* Left slot: month navigation (hidden when year picker active) */}
        <Box display="flex" alignItems="center">
          {!showYearPicker && (
            <>
              <Box display="flex" alignItems="center" sx={{ mr: 0.5 }}>
                <IconButton size="small" onClick={onPreviousMonth} aria-label="Previous month">
                  <KeyboardArrowLeftIcon />
                </IconButton>
                <IconButton size="small" onClick={onNextMonth} aria-label="Next month">
                  <KeyboardArrowRightIcon />
                </IconButton>
              </Box>
              <Box sx={{
                fontWeight: 700,
                lineHeight: 1.2,
                [theme.breakpoints.down('md')]: {
                  marginRight: 1
                }
              }}
              >
                <FormattedMessage id={`calendar.month.${currentDate.getMonth()}`} />
              </Box>
            </>
          )}
        </Box>

        {/* Right slot: year dropdown (always visible, toggles on click) */}
        <Box
          onClick={onYearClick}
          sx={{
            display: 'flex',
            alignItems: 'center',
            cursor: 'pointer',
            fontWeight: 700,
            color: 'text.primary',
            '&:hover': { color: 'primary.main' },
          }}
        >
          {currentDate.getFullYear()}
          {showYearPicker ? (
            <ArrowDropUpIcon sx={{ ml: 0.5 }} />
          ) : (
            <ArrowDropDownIcon sx={{ ml: 0.5 }} />
          )}
        </Box>
      </Box>
    </Box>
  );
};