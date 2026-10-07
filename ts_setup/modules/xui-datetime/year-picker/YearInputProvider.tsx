import React from 'react';



export interface YearInputContextType {
  disabled: boolean;
  inputValue: string;
  isError: boolean;
  handleInputChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  handleClear: () => void;
  handleOpen: () => void;
}

const YearInputContext = React.createContext<YearInputContextType | null>(null);

export const useYearInput = (): YearInputContextType => {
  const ctx = React.useContext(YearInputContext);
  if (!ctx) {
    throw new Error('useYearInput must be used within a YearInputProvider');
  }
  return ctx;
};

export const YearInputProvider: React.FC<{
  value: number | null;
  disabled?: boolean;
  onChange: (year: number | null) => void;
  onOpen: () => void;
  children: React.ReactNode;
}> = ({ value, disabled, onChange, onOpen, children }) => {
  const [inputValue, setInputValue] = React.useState<string>(value !== null ? String(value) : '');
  const [isError, setIsError] = React.useState(false);

  React.useEffect(() => {
    setInputValue(value !== null ? String(value) : '');
    if (value !== null) {
      setIsError(false);
    }
  }, [value]);

  function handleInputChange(event: React.ChangeEvent<HTMLInputElement>) {
    const digits = event.target.value.replace(/\D/g, '').slice(0, 4);
    setInputValue(digits);
    if (digits.length === 0) {
      setIsError(false);
      onChange(null);
    } else {
      const year = parseInt(digits, 10);
      setIsError(false);
      onChange(year);
    }
  }

  function handleClear() {
    setInputValue('');
    setIsError(false);
    onChange(null);
  }

  const contextValue: YearInputContextType = {
    disabled: disabled === true,
    inputValue,
    isError,
    handleInputChange,
    handleClear,
    handleOpen: onOpen,
  };

  return (
    <YearInputContext.Provider value={contextValue}>
      {children}
    </YearInputContext.Provider>
  );
};
