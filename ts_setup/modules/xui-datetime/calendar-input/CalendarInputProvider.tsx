import React from "react";
import { DateInputStateMachine, DateField } from "./DateInputStateMachine";

export interface CalendarInputProps {
  value?: Date | null;
  onChange: (date: Date | null) => void;
  onCalendarOpen?: () => void;
  placeholder?: string;
  disabled?: boolean;
  className: string;
}

export interface CalendarInputContextType {
  // Machine state
  machine: DateInputStateMachine;
  
  // Refs for focus management
  dayRef: React.RefObject<HTMLInputElement>;
  monthRef: React.RefObject<HTMLInputElement>;
  yearRef: React.RefObject<HTMLInputElement>;
  
  // Actions
  typeDayDigit: (value: string) => void;
  typeMonthDigit: (value: string) => void;
  typeYearDigit: (value: string) => void;
  focusField: (field: DateField) => void;
  blurField: (field: DateField) => void;
  clear: () => void;
  handleKeyDown: (e: React.KeyboardEvent<HTMLInputElement>, field: DateField) => void;
  handleContainerClick: (e: React.MouseEvent) => void;
  handleCalendarOpen: () => void;
}

const CalendarInputContext = React.createContext<CalendarInputContextType | null>(null);

export const useCalendarInput = () => {
  const context = React.useContext(CalendarInputContext);
  if (!context) {
    throw new Error('useCalendarInput must be used within a CalendarInputProvider');
  }
  return context;
};

export const CalendarInputProvider: React.FC<{
  children: React.ReactNode;
  value?: Date | null;
  onChange: (date: Date | null) => void;
  onCalendarOpen?: () => void;
}> = ({ children, value, onChange, onCalendarOpen }) => {
  // THE MAGIC: Single state machine instance
  const [machine, setMachine] = React.useState(() => new DateInputStateMachine().setFromDate(value || null));
  
  const dayRef = React.useRef<HTMLInputElement>(null);
  const monthRef = React.useRef<HTMLInputElement>(null);
  const yearRef = React.useRef<HTMLInputElement>(null);

  // Focus management helper
  const focusAndSelect = (ref: React.RefObject<HTMLInputElement>) => {
    if (ref.current) {
      ref.current.focus();
      setTimeout(() => {
        if (ref.current) {
          ref.current.select();
        }
      }, 0);
    }
  };

  // Handle external value changes (like from a calendar picker)
  React.useEffect(() => {
    setMachine(prev => {
      if (prev.resultDate?.getTime() !== value?.getTime()) {
        return prev.setFromDate(value || null);
      }
      return prev;
    });
  }, [value]);

  // Handle auto-advance focus
  React.useEffect(() => {
    if (machine.shouldAutoAdvance) {
      if (machine.focusedField === 'month') {
        focusAndSelect(monthRef);
      } else if (machine.focusedField === 'year') {
        focusAndSelect(yearRef);
      }
    }
  }, [machine.shouldAutoAdvance, machine.focusedField]);

  // Handle date completion
  React.useEffect(() => {
    if(!machine.isUserChange) {
      return;
    }
    if (machine.state === 'valid' && machine.resultDate) {
      const finalDate = new Date(machine.resultDate);
      finalDate.setHours(15);
      onChange(finalDate);
    } else if (machine.state === 'empty') {
      onChange(null);
    }
  }, [machine.state, machine.resultDate, machine.isUserChange]);

  // Actions - these are the ONLY ways to mutate state
  const typeDayDigit = (value: string) => {
    setMachine(prev => prev.typeDayDigit(value));
  };

  const typeMonthDigit = (value: string) => {
    setMachine(prev => prev.typeMonthDigit(value));
  };

  const typeYearDigit = (value: string) => {
    setMachine(prev => prev.typeYearDigit(value));
  };

  const focusField = (field: DateField) => {
    setMachine(prev => prev.focusField(field));
  };

  const blurField = (field: DateField) => {
    setTimeout(() => {
      setMachine(prev => {
        if (prev.focusedField !== field) {
          return prev;
        }
        return prev.blurField(field)
      });

    }, 250);
  };

  const clear = () => {
    setMachine(prev => prev.clear());
    focusAndSelect(dayRef);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>, field: DateField) => {

    
    if (e.key === 'Backspace' && e.currentTarget.value === '') {
      if (field === 'month') {
        focusAndSelect(dayRef);
      } else if (field === 'year') {
        focusAndSelect(monthRef);
      }
    }

    if (e.key === 'ArrowRight') {
      if (field === 'day') {
        focusAndSelect(monthRef);
      } else if (field === 'month') {
        focusAndSelect(yearRef);
      }
    }

    if (e.key === 'ArrowLeft') {
      if (field === 'month') {
        focusAndSelect(dayRef);
      } else if (field === 'year') {
        focusAndSelect(monthRef);
      }
    }

    if (e.key === 'Enter') {
      if (machine.state === 'valid' && machine.resultDate) {
        onChange(machine.resultDate);
        if (yearRef.current) {
          yearRef.current.blur();
        } else if (monthRef.current) {
          monthRef.current.blur();
        } else if (dayRef.current) {
          dayRef.current.blur();
        }
      }
    }
    
  };

  const handleContainerClick = (e: React.MouseEvent) => {
    if ((e.target as HTMLElement).tagName === 'INPUT') {
      return;
    }

    if (!machine.day) {
      focusAndSelect(dayRef);
    } else if (!machine.month) {
      focusAndSelect(monthRef);
    } else if (!machine.year) {
      focusAndSelect(yearRef);
    } else {
      focusAndSelect(dayRef);
    }
  };

  const handleCalendarOpen = () => {
    if (onCalendarOpen) {
      onCalendarOpen();
    }
  };

  const contextValue: CalendarInputContextType = {
    machine,
    dayRef,
    monthRef,
    yearRef,
    typeDayDigit,
    typeMonthDigit,
    typeYearDigit,
    focusField,
    blurField,
    clear,
    handleKeyDown,
    handleContainerClick,
    handleCalendarOpen,
  };

  return (
    <CalendarInputContext.Provider value={contextValue}>
      {children}
    </CalendarInputContext.Provider>
  );
};