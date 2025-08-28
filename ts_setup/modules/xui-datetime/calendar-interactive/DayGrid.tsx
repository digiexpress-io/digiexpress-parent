import { Box, Button, Grid2, Typography, alpha } from "@mui/material";
import { FormattedMessage } from "react-intl";
import { SQUARE_WIDTH } from "./calendar_constants";

const WIDTH = `${SQUARE_WIDTH}px`;

// Day Grid Component
export const DayGrid: React.FC<{
  currentDate: Date;
  selectedDate?: Date | null;
  onDateSelect: (date: Date) => void;
  minDate?: Date;
  maxDate?: Date;
}> = ({ currentDate, selectedDate, onDateSelect, minDate, maxDate }) => {
  
  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();
  
  // Get first day of month and how many days in month
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const daysInMonth = lastDay.getDate();
  
  // Convert Sunday-based (0-6) to Monday-based (0-6)
  // Sunday = 0 becomes 6, Monday = 1 becomes 0, etc.
  const startingDayOfWeek = (firstDay.getDay() + 6) % 7;
  
  // Create array of all days to display
  const days: (number | null)[] = [];
  
  // Add empty cells for days before month starts
  for (let i = 0; i < startingDayOfWeek; i++) {
    days.push(null);
  }
  
  // Add all days in month
  for (let day = 1; day <= daysInMonth; day++) {
    days.push(day);
  }
  
  const isDateDisabled = (day: number): boolean => {
    const date = new Date(year, month, day);
    if (minDate && date < minDate) return true;
    if (maxDate && date > maxDate) return true;
    return false;
  };
  
  const isDateSelected = (day: number): boolean => {
    if (!selectedDate) return false;
    return selectedDate.getDate() === day &&
           selectedDate.getMonth() === month &&
           selectedDate.getFullYear() === year;
  };
  
  const handleDateClick = (day: number) => {
    if (isDateDisabled(day)) return;
    const date = new Date(year, month, day);
    onDateSelect(date);
    };

    return (
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 1 }}>
        {/* Day headers */}
        {[1, 2, 3, 4, 5, 6, 0].map((day) => (
          <Box key={day} textAlign="center">
            <Typography variant="button" color="primary.main" fontWeight="bold">
              <FormattedMessage id={`calendar.day.${day}`} />
            </Typography>
          </Box>
        ))}

        {days.map((day, index) => (
          <Box key={`day_${day ?? 'filler_' + index}`} textAlign="center">
            {day && (
              <Button
                onClick={() => handleDateClick(day)}
                disabled={isDateDisabled(day)}
                sx={(theme) => ({
                  minWidth: WIDTH,
                  width: WIDTH,
                  height: WIDTH,
                  borderRadius: '50%',
                  fontWeight: 500,
                  color: isDateSelected(day)
                    ? theme.palette.common.white
                    : theme.palette.text.primary,
                  backgroundColor: isDateSelected(day)
                    ? theme.palette.primary.main
                    : 'transparent',
                  '&:hover': {
                    backgroundColor: isDateSelected(day)
                      ? theme.palette.primary.dark
                      : theme.palette.action.hover,
                  },
                  '&.Mui-disabled': {
                    color: theme.palette.text.disabled,
                    backgroundColor: 'transparent',
                  },
                })}
              >
                {day}
              </Button>
            )}
          </Box>
        ))}
      </Box>
    );

  };

