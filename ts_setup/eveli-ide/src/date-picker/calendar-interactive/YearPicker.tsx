import { Box, Button, Grid2, IconButton, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import CloseIcon from '@mui/icons-material/Close';

// Year Picker Component
export const YearPicker: React.FC<{
  currentYear: number;
  onYearSelect: (year: number) => void;
  onClose: () => void;
  minDate?: Date;
  maxDate?: Date;
}> = ({ currentYear, onYearSelect, onClose, minDate, maxDate }) => {
  const startYear = currentYear - 10;
  const endYear = currentYear + 10;
  const years = [];
  
  for (let year = startYear; year <= endYear; year++) {
    years.push(year);
  }
  
  const isYearDisabled = (year: number): boolean => {
    if (minDate && year < minDate.getFullYear()) return true;
    if (maxDate && year > maxDate.getFullYear()) return true;
    return false;
  };


  
  return (
    <>
      
      <Box display='flex' flexDirection='row' alignItems='center' justifyContent='center'>
        <Typography variant='h3'><FormattedMessage id='calendar.select.year' defaultMessage='Select year'/></Typography>
        <div>
          <IconButton 
            onClick={onClose}
            className='p-2 hover:bg-gray-100 rounded'
            aria-label='Close year picker'>
            <CloseIcon />
          </IconButton>
        </div>
      </Box>
      
      <Grid2 container sx={{
        display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 1 
      }}>
        {years.map((year) => (
          <Grid2>
            <Button
              key={year}
              onClick={() => onYearSelect(year)}
              disabled={isYearDisabled(year)}
              variant={year === currentYear ? undefined : 'text' }
              sx={{
                color: year === currentYear ? undefined : ((theme) => theme.palette.text.primary), 
              }}>
              {year}
            </Button>
          </Grid2>
        ))}
      </Grid2>
    </>
  );
}