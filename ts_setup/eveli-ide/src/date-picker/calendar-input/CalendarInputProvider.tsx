
import React from "react";


export interface CalendarInputProps {
  value?: Date | null;
  onChange: (date: Date | null) => void;
  onCalendarOpen?: () => void;
  placeholder?: string;
  disabled?: boolean;
  className: string;
}

// Context Types
export interface CalendarInputContextType {
  // State
  day: string;
  month: string;
  year: string;
  isValid: boolean;

  disabled: boolean;
  
  // Refs
  dayRef: React.RefObject<HTMLInputElement>;
  monthRef: React.RefObject<HTMLInputElement>;
  yearRef: React.RefObject<HTMLInputElement>;
  
  focusedField: 'day' | 'month' | 'year' | null;


  // Handlers
  handleDayChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  handleMonthChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  handleYearChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  handleKeyDown: (e: React.KeyboardEvent<HTMLInputElement>, field: 'day' | 'month' | 'year') => void;
  handleContainerClick: (e: React.MouseEvent) => void;
  
  handleFocus: (field: 'day' | 'month' | 'year') => void;
  handleBlur: (field: 'day' | 'month' | 'year', e: React.FocusEvent) => void;

  handleClear: () => void;
  handleCalendarOpen: () => void;
}

const CalendarInputContext = React.createContext<CalendarInputContextType | null>(null);

// Custom hook to use the context
export const useCalendarInput = () => {
  const context = React.useContext(CalendarInputContext);
  if (!context) {
    throw new Error('useCalendarInput must be used within a CalendarInputProvider');
  }
  return context;
};

// Provider component
export const CalendarInputProvider: React.FC<{
  children: React.ReactNode;
  value?: Date | null;
  onChange: (date: Date | null) => void;
  onCalendarOpen?: () => void;
  disabled?: boolean;
}> = ({ children, value, onChange, onCalendarOpen, disabled = false }) => {
  const [day, setDay] = React.useState('');
  const [month, setMonth] = React.useState('');
  const [year, setYear] = React.useState('');
  const [isValid, setIsValid] = React.useState(true);
  const [focusedField, setFocusedField] = React.useState<'day' | 'month' | 'year' | null>(null);


  const dayRef = React.useRef<HTMLInputElement>(null);
  const monthRef = React.useRef<HTMLInputElement>(null);
  const yearRef = React.useRef<HTMLInputElement>(null);

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

  // Update internal state when value prop changes
  React.useEffect(() => {
    if (value) {
      const d = value.getDate().toString().padStart(2, '0');
      const m = (value.getMonth() + 1).toString().padStart(2, '0');
      const y = value.getFullYear().toString();
      setDay(d);
      setMonth(m);
      setYear(y);
    } else {
      setDay('');
      setMonth('');
      setYear('');
    }
    setIsValid(true);
  }, [value]);

  // Validate and create date
  const validateAndSetDate = (dayVal: string, monthVal: string, yearVal: string) => {
    if (!dayVal || !monthVal || !yearVal) {
      setIsValid(true);
      return;
    }

    const dayNum = parseInt(dayVal);
    const monthNum = parseInt(monthVal);
    const yearNum = parseInt(yearVal);

    // Basic range validation
    if (dayNum < 1 || dayNum > 31 || monthNum < 1 || monthNum > 12 || yearNum < 1970 || yearNum > 2100) {
      setIsValid(false);
      return;
    }

    // Create date and validate it's real
    const date = new Date(yearNum, monthNum - 1, dayNum);
    const isValidDate = date.getFullYear() === yearNum &&
      date.getMonth() === monthNum - 1 &&
      date.getDate() === dayNum;

    if (isValidDate) {
      setIsValid(true);
      onChange(date);
      console.log('Date input completed, should close calendar popup');
    } else {
      setIsValid(false);
    }
  };

  const handleDayChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\D/g, '').slice(0, 2);
    const previousLength = day.length;
    setDay(value);

    if (value.length === 2 && value.length > previousLength) {
      const dayNum = parseInt(value);
      if (dayNum >= 1 && dayNum <= 31) {
        focusAndSelect(monthRef);
      }
    }

    validateAndSetDate(value, month, year);
  };

  const handleMonthChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\D/g, '').slice(0, 2);
    const previousLength = month.length;
    setMonth(value);

    if (value.length === 2 && value.length > previousLength) {
      const monthNum = parseInt(value);
      if (monthNum >= 1 && monthNum <= 12) {
        focusAndSelect(yearRef);
      }
    }

    validateAndSetDate(day, value, year);
  };

  const handleYearChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\D/g, '').slice(0, 4);
    setYear(value);
    validateAndSetDate(day, month, value);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>, field: 'day' | 'month' | 'year') => {
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
  };

  const handleContainerClick = (e: React.MouseEvent) => {
    if (disabled) return;

    if ((e.target as HTMLElement).tagName === 'INPUT') {
      return;
    }

    if (!day) {
      focusAndSelect(dayRef);
    } else if (!month) {
      focusAndSelect(monthRef);
    } else if (!year) {
      focusAndSelect(yearRef);
    } else {
      focusAndSelect(dayRef);
    }
  };

  const handleFocus = (field: 'day' | 'month' | 'year') => {
    setFocusedField(field);
  };

  const handleBlur = (field: 'day' | 'month' | 'year', e: React.FocusEvent) => {
    setTimeout(() => {
      const activeElement = document.activeElement;
      const isStillInComponent = dayRef.current === activeElement || 
                                monthRef.current === activeElement || 
                                yearRef.current === activeElement;
      
      if (!isStillInComponent) {
        setFocusedField(null);
      }
      // If focus moved to another field, handleFocus will be called automatically
    }, 0);
  };


  const handleClear = () => {
    setDay('');
    setMonth('');
    setYear('');
    setIsValid(true);
    onChange(null);
    focusAndSelect(dayRef);
  };

  const handleCalendarOpen = () => {
    if (onCalendarOpen) {
      onCalendarOpen();
    }
  };

  const contextValue: CalendarInputContextType = {
    // State
    day,
    month,
    year,
    isValid,
    focusedField,
    disabled,
    
    // Refs
    dayRef,
    monthRef,
    yearRef,
    
    // Handlers
    handleDayChange,
    handleMonthChange,
    handleYearChange,
    handleKeyDown,
    handleContainerClick,
    handleFocus,
    handleBlur,
    handleClear,
    handleCalendarOpen,
  };

  return (
    <CalendarInputContext.Provider value={contextValue}>
      {children}
    </CalendarInputContext.Provider>
  );
}
