import React from "react";
import { Box } from "@mui/material";
import { useIntl } from "react-intl";

import { Input } from "./Input";
import { useCalendarInput } from "./CalendarInputProvider";


// Field components using context
const DayField: React.FC = () => {
  const intl = useIntl();
  const { disabled, machine, dayRef, handleKeyDown, focusField, blurField, typeDayDigit } = useCalendarInput();
  return (
    <Input
      disabled={disabled}
      ref={dayRef}
      type="text"
      value={machine.day}
      onFocus={() => focusField('day')}
      onBlur={() => blurField('day')}
      onChange={(e) => typeDayDigit(e.target.value)}
      onKeyDown={(e) => { e.stopPropagation(); handleKeyDown(e, 'day'); }}
      onClick={(e) => e.stopPropagation()}
      placeholder={intl.formatMessage({ id: 'xui.calendarInput.mask.placeholder.day', defaultMessage: 'dd' })}
      maxLength={2}
      style={{ width: '2ch', textAlign: 'center' }}
    />
  );
};

const MonthField: React.FC = () => {
  const intl = useIntl();
  const { disabled, machine, monthRef, handleKeyDown, focusField, blurField, typeMonthDigit } = useCalendarInput();
  return (
    <Input
      disabled={disabled}
      ref={monthRef}
      type="text"
      value={machine.month}
      onFocus={() => focusField('month')}
      onBlur={() => blurField('month')}
      onChange={(e) => typeMonthDigit(e.target.value)}
      onKeyDown={(e) => { e.stopPropagation(); handleKeyDown(e, 'month'); }}
      onClick={(e) => e.stopPropagation()}
      placeholder={intl.formatMessage({ id: 'xui.calendarInput.mask.placeholder.month', defaultMessage: 'mm' })}
      maxLength={2}
      style={{ width: '3ch', textAlign: 'center' }}
    />
  );
};

const YearField: React.FC = () => {
  const intl = useIntl();
  const { machine, yearRef, disabled, handleKeyDown, focusField, blurField, typeYearDigit } = useCalendarInput();
  return (
    <Input
      disabled={disabled}
      ref={yearRef}
      type="text"
      value={machine.year}
      onFocus={() => focusField('year')}
      onBlur={() => blurField('year')}
      onChange={(e) => typeYearDigit(e.target.value)}
      onKeyDown={(e) => { e.stopPropagation(); handleKeyDown(e, 'year'); }}
      onClick={(e) => e.stopPropagation()}
      placeholder={intl.formatMessage({ id: 'xui.calendarInput.mask.placeholder.year', defaultMessage: 'yyyy' })}
      maxLength={4}
      style={{ width: '4ch', textAlign: 'center' }}
    />
  );
};

const DateSeparator: React.FC = () => (
  <span style={{ color: '#999' }}>.</span>
);

const ErrorMessage: React.FC = () => {
  const { machine } = useCalendarInput();

  if (machine.isValid) return null;
  
  // todo
  return (<div></div>);
};


// Container component that uses context
const CalendarInputInternal: React.FC<{ className: string }> = ({ className }) => {
  const { handleContainerClick } = useCalendarInput();
  
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