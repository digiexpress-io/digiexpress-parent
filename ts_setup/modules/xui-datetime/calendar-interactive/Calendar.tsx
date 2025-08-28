import React from 'react';

import { CalendarHeader } from './CalendarHeader';
import { YearPicker } from './YearPicker';
import { DayGrid } from './DayGrid';
import { CardWrapper } from './CardWrapper';


// Calendar Props
export interface CalendarProps {
  initialDate: Date;
  selectedDate?: Date | null;
  minDate?: Date;
  maxDate?: Date;
  inline?: boolean | undefined;
  onDateSelect: (date: Date) => void;
  onClose?: () => void;
}

// Main Calendar Component
export const Calendar: React.FC<CalendarProps> = ({
  initialDate,
  selectedDate,
  minDate, maxDate,
  inline,
  onDateSelect,
  onClose
}) => {
  const [currentDate, setCurrentDate] = React.useState(new Date(initialDate));
  const [showYearPicker, setShowYearPicker] = React.useState(false);

  // Handle keyboard navigation
  React.useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (showYearPicker) {
          setShowYearPicker(false);
        } else {
          onClose?.();
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [showYearPicker, onClose]);

  const handlePreviousMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
  };

  const handleNextMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
  };

  const handlePreviousYear = () => {
    setCurrentDate(new Date(currentDate.getFullYear() - 1, currentDate.getMonth(), 1));
  };

  const handleNextYear = () => {
    setCurrentDate(new Date(currentDate.getFullYear() + 1, currentDate.getMonth(), 1));
  };

  const handleYearSelect = (year: number) => {
    setCurrentDate(new Date(year, currentDate.getMonth(), 1));
    setShowYearPicker(false);
  };


  return (
    <CardWrapper inline={inline ? true : false}>
      <CalendarHeader
        currentDate={currentDate}
        onPreviousMonth={handlePreviousMonth}
        onNextMonth={handleNextMonth}
        onYearClick={() => setShowYearPicker(prev => !prev)}
        showYearPicker={showYearPicker}
      />
      {showYearPicker ? (
        <YearPicker
          currentYear={currentDate.getFullYear()}
          onYearSelect={handleYearSelect}
          onClose={() => setShowYearPicker(false)}
          minDate={minDate}
          maxDate={maxDate}
        />
      ) : (
        <DayGrid
          currentDate={currentDate}
          selectedDate={selectedDate}
          onDateSelect={onDateSelect}
          minDate={minDate}
          maxDate={maxDate}
        />
      )}
    </CardWrapper>
  );
};