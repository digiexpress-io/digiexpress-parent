import { Typography, Box, IconButton, Button, Divider } from '@mui/material';
import KeyboardArrowLeftIcon from '@mui/icons-material/KeyboardArrowLeft';
import KeyboardArrowRightIcon from '@mui/icons-material/KeyboardArrowRight';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import ArrowDropUpIcon from '@mui/icons-material/ArrowDropUp';
import React from 'react';
import { FormattedMessage } from 'react-intl';
import { SQUARE_WIDTH } from './calendar_constants';

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
  const width = (SQUARE_WIDTH + 8) * 7;

  return (
    <Box minWidth={width} width={width}>
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
              <Box
                sx={{
                  fontSize: '1.2rem',
                  fontWeight: 700,
                  letterSpacing: '-0.25px',
                  lineHeight: 1.2,
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
            fontSize: '1.2rem',
            fontWeight: 700,
            letterSpacing: '-0.25px',
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