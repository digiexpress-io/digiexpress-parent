import React from 'react';
import { getMessages } from './intl';
import { Calendar } from './Calendar';
import { IntlProvider } from 'react-intl';
import { DateTime } from 'luxon';

// Date Names Context
interface DateNamesContextType {
  incomplete: boolean;
  error: string | null;
}

const DateNamesContext = React.createContext<DateNamesContextType>({
  incomplete: false,
  error: null
});

const NOW = DateTime.now();


export interface CalendarProviderProps {

  value: Date | null;
  locale: string;
  open: boolean;

  onClose: () => void;
  onChange: (value: Date | null) => void;
}

export const CalendarProvider: React.FC<CalendarProviderProps> = ({
  locale,
  value: selectedDate,
  open: showCalendar,
  onChange, onClose,
}) => {
    const messages = React.useMemo(() => getMessages(locale), [locale]);

    if(!showCalendar) {
      return (<></>)
    }

    return (
      <IntlProvider locale={locale} messages={messages}>
        <DateNamesContext.Provider value={{
          incomplete: false,
          error: null
        }}>
          <Calendar
            initialDate={selectedDate ?? NOW.toJSDate()}
            selectedDate={selectedDate}
            onDateSelect={onChange}
            onClose={onClose}
            minDate={ NOW.minus({ years: 10 }).toJSDate() }
            maxDate={ NOW.plus({ years: 2 }).toJSDate() }
          />
        </DateNamesContext.Provider>
      </IntlProvider>
    );
  };