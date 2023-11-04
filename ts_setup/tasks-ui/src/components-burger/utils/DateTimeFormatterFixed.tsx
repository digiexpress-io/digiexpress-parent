import moment from 'moment';
import React from 'react';

interface DateTimeFormatterProps {
    timestamp: string | undefined;
}

const formatDateTime = (timestamp: string | undefined) => {
    if (timestamp) {
        const date = new Date(timestamp);
        return moment.utc(date).local().format('DD/MM/YYYY HH:mm:ss');
    }
    return "";
}

const DateTimeFormatterFixed: React.FC<DateTimeFormatterProps> = ({ timestamp }) => {
    return (
        <>
            {formatDateTime(timestamp)}
        </>
    )
}

export type { DateTimeFormatterProps }
export { DateTimeFormatterFixed, formatDateTime }
