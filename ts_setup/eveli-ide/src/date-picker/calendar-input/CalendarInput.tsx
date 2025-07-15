import { Box, useTheme } from "@mui/material";
import React from "react";
import { Input } from "./Input";
import { useCalendarInput } from "./CalendarInputProvider";

// Field components using context
const DayField: React.FC = () => {
  const { machine, dayRef, handleKeyDown, focusField, blurField, typeDayDigit } = useCalendarInput();
  return (
    <Input
      ref={dayRef}
      type="text"
      value={machine.day}
      onFocus={() => focusField('day')}
      onBlur={(e) => blurField('day')}
      onChange={(e) => typeDayDigit(e.target.value)}
      onKeyDown={(e) => {
        e.stopPropagation();
        handleKeyDown(e, 'day');
      }}
      onClick={(e) => e.stopPropagation()}
      placeholder="dd"
      style={{ width: '4ch' }}
      maxLength={2}
    />
  );
};

const MonthField: React.FC = () => {
  const { machine, monthRef, handleKeyDown, focusField, blurField, typeMonthDigit } = useCalendarInput();
  
  return (
    <Input
      ref={monthRef}
      type="text"
      value={machine.month}
      onFocus={() => focusField('month')}
      onBlur={(e) => blurField('month')}
      onChange={(e) => typeMonthDigit(e.target.value)}
      onKeyDown={(e) => {
        e.stopPropagation();
        handleKeyDown(e, 'month');
      }}
      onClick={(e) => e.stopPropagation()}
      placeholder="mm"
      style={{ width: '4ch' }}
      maxLength={2}
    />
  );
};

const YearField: React.FC = () => {
  const { machine, yearRef, handleKeyDown, focusField, blurField, typeYearDigit } = useCalendarInput();
  
  return (
    <Input
      ref={yearRef}
      type="text"
      value={machine.year}
      onFocus={() => focusField('year')}
      onBlur={(e) => blurField('year')}
      onChange={(e) => typeYearDigit(e.target.value)}
      onKeyDown={(e) => {
        e.stopPropagation();
        handleKeyDown(e, 'year')
      }}
      onClick={(e) => e.stopPropagation()}
      placeholder="yyyy"
      style={{ width: '8ch' }}
      maxLength={4}
    />
  );
};

const DateSeparator: React.FC = () => {
  
  return (
    <span 
      className="text-gray-400 mx-1" 
      style={{ color: '#999' }}
    >
      .
    </span>
  );
};

const ErrorMessage: React.FC = () => {
  const { machine } = useCalendarInput();
  
  if (machine.isValid) return null;
  
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