import { Box, useTheme } from "@mui/material";
import React from "react";
import { Input } from "./Input";
import { useCalendarInput } from "./CalendarInputProvider";

// Field components using context
const DayField: React.FC = () => {
  const { day, dayRef, handleDayChange, handleKeyDown, handleFocus, handleBlur, disabled } = useCalendarInput();
  
  return (
    <Input
      ref={dayRef}
      type="text"
      value={day}
      onFocus={() => handleFocus('day')}
      onBlur={(e) => handleBlur('day', e)}
      onChange={handleDayChange}
      onKeyDown={(e) => handleKeyDown(e, 'day')}
      onClick={(e) => e.stopPropagation()}
      placeholder="dd"
      disabled={disabled}
      style={{ width: '4ch' }}
      maxLength={2}
    />
  );
};

const MonthField: React.FC = () => {
  const { month, monthRef, handleMonthChange, handleKeyDown, handleFocus, handleBlur, disabled } = useCalendarInput();
  
  return (
    <Input
      ref={monthRef}
      type="text"
      value={month}
      onFocus={() => handleFocus('month')}
      onBlur={(e) => handleBlur('month', e)}
      onChange={handleMonthChange}
      onKeyDown={(e) => handleKeyDown(e, 'month')}
      onClick={(e) => e.stopPropagation()}
      placeholder="mm"
      disabled={disabled}
      style={{ width: '4ch' }}
      maxLength={2}
    />
  );
};

const YearField: React.FC = () => {
  const { year, yearRef, handleYearChange, handleKeyDown, handleFocus, handleBlur, disabled } = useCalendarInput();
  
  return (
    <Input
      ref={yearRef}
      type="text"
      value={year}
      onFocus={() => handleFocus('year')}
      onBlur={(e) => handleBlur('year', e)}
      onChange={handleYearChange}
      onKeyDown={(e) => handleKeyDown(e, 'year')}
      onClick={(e) => e.stopPropagation()}
      placeholder="yyyy"
      disabled={disabled}
      style={{ width: '8ch' }}
      maxLength={4}
    />
  );
};

const DateSeparator: React.FC = () => {
  const { disabled } = useCalendarInput();
  
  return (
    <span 
      className="text-gray-400 mx-1" 
      style={{ color: disabled ? '#00000042' : '#999' }}
    >
      .
    </span>
  );
};

const ErrorMessage: React.FC = () => {
  const { isValid } = useCalendarInput();
  
  if (isValid) return null;
  
  return (
    <div className="absolute top-full left-0 mt-1 text-sm text-red-500">
      Invalid date
    </div>
  );
};


// Container component that uses context
const CalendarInputInternal: React.FC<{ className: string }> = ({ className }) => {
  const { handleContainerClick } = useCalendarInput();
  const theme = useTheme();
  
  return (
    <Box
      display="flex"
      flexDirection="row"
      className={className}
      onClick={handleContainerClick}
    >
      <DayField />
      <DateSeparator />
      <MonthField />
      <DateSeparator />
      <YearField />
      <ErrorMessage />
    </Box>
  );
}

export const CalendarInput = React.forwardRef<HTMLInputElement, { className: string }>(
  (props, ref) => {
    return (<div ref={ref}><CalendarInputInternal {...props} /></div>);
  }
)