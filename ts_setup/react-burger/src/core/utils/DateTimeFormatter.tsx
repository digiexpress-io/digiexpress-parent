import moment from 'moment';
import React from 'react';

interface DateTimeFormatProps {
    timestamp: string | undefined;
}

const formatDateTime = (timestamp: string | undefined) => {
    if (timestamp) {
      const date = new Date(timestamp);
      return moment.utc(date).local().format('DD/MM/YYYY HH:mm:ss');
    }
    return "";
}

const DateTimeFormat: React.FC<DateTimeFormatProps> = ({ timestamp }) => {
    return (
        <>
            {formatDateTime(timestamp)}
        </>
    )
}

export type { DateTimeFormatProps }
export { DateTimeFormat, formatDateTime }
