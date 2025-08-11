import { Typography, Box, IconButton, Button, Divider } from '@mui/material';

import KeyboardDoubleArrowLeftIcon from '@mui/icons-material/KeyboardDoubleArrowLeft';
import KeyboardDoubleArrowRightIcon from '@mui/icons-material/KeyboardDoubleArrowRight';

import KeyboardArrowLeftIcon from '@mui/icons-material/KeyboardArrowLeft';
import KeyboardArrowRightIcon from '@mui/icons-material/KeyboardArrowRight';

import React from 'react';
import { FormattedMessage } from 'react-intl';

// Calendar Header Component
export const CalendarHeader: React.FC<{
  currentDate: Date;
  onPreviousMonth: () => void;
  onNextMonth: () => void;
  onPreviousYear: () => void;
  onNextYear: () => void;
  onYearClick: () => void;

}> = ({ currentDate, onPreviousMonth, onNextMonth, onPreviousYear, onNextYear, onYearClick }) => {
  
  const width = (48 + 8) * 7;
  return (
    <Box minWidth={width} width={width} >
      <Box display='flex' paddingRight={1} alignItems='center'>

        <div className="flex items-center space-x-1">
          <IconButton onClick={onPreviousYear} aria-label="Previous year">
            <KeyboardDoubleArrowLeftIcon />
          </IconButton>

          <IconButton onClick={onPreviousMonth} aria-label="Previous month">
            <KeyboardArrowLeftIcon />
          </IconButton>
        </div>

        <Box flexGrow={1} display='flex' alignItems='center' justifyContent='center'>
          <Box display='flex' flexDirection='row' alignItems='center' justifyContent='center'>
            <Typography variant='h4'>
              <FormattedMessage id={`calendar.month.${currentDate.getMonth()}`}/>
            </Typography>
            <Box px={1}/>
            <Button onClick={onYearClick}>
              {currentDate.getFullYear()}
            </Button>
          </Box>
        </Box>

        <div className="flex items-center space-x-1">
          <IconButton onClick={onNextMonth} aria-label="Next month">
            <KeyboardArrowRightIcon />
          </IconButton>

          <IconButton onClick={onNextYear} aria-label="Next year">
            <KeyboardDoubleArrowRightIcon />
          </IconButton>
        </div>
      </Box>
      <Divider sx={{my: 1}}/>
    </Box>
  );
};