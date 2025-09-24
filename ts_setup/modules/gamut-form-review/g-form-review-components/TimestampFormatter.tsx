import type React from 'react';

type TimestampFormat = 'date' | 'datetime' | 'time';

const isTime = (value: string) => {
  return value.length === 5 && value.indexOf(':') === 2;
}

function convertToUTCPlus3(utcTime: Date) {

  const utcPlus3TimeInMs = utcTime.getTime() + 3 * 60 * 60 * 1000;
  const utcPlus3Date = new Date(utcPlus3TimeInMs);
  const formattedUTCPlus3Time = utcPlus3Date.toISOString();

  return formattedUTCPlus3Time;
}

const FormattedDate: React.FC<{ value: string }> = ({ value }) => {
  const date = new Date(value);
  const datePlus3 = new Date(convertToUTCPlus3(date))
  const endsWithZ = value.endsWith('Z')
  if (endsWithZ) {
    return (
      <span style={{ color: 'red' }}>{date.toLocaleDateString('fi-FI', { day: '2-digit', month: '2-digit', year: 'numeric', timeZone: 'Europe/Helsinki' })}</span>
    )
  }
  return (
    <span style={{ color: 'red' }}>{datePlus3.toLocaleDateString('fi-FI', { day: '2-digit', month: '2-digit', year: 'numeric' })}</span>
  )
}

const FormattedTime: React.FC<{ value: string }> = ({ value }) => {
  const endsWithZ = value.endsWith('Z')
  if (isTime(value)) {
    return (
      <span>{value.replace(':', '.')}</span>
    )
  }
  const date = new Date(value);
  const datePlus3 = new Date(convertToUTCPlus3(date))

  if (endsWithZ) {
    return (
      <span style={{ color: 'blue' }}>{date.toLocaleTimeString('fi-FI', { hour: '2-digit', minute: '2-digit', timeZone: 'Europe/Helsinki' })}</span>
    )
  }
  return (
    <span style={{ color: 'blue' }}>{datePlus3.toLocaleTimeString('fi-FI', { hour: '2-digit', minute: '2-digit' })}</span>
  )
}

interface TimestampFormatterProps {
  timestamp: string,
  breakLines?: boolean,
  format: TimestampFormat
}

const TimestampFormatter: React.FC<TimestampFormatterProps> = ({ timestamp, breakLines, format }) => {
  if (format === 'date') {
    return (
      <FormattedDate value={timestamp} />
    )
  }
  if (format === 'time') {
    return (
      <FormattedTime value={timestamp} />
    )
  }
  return (
    <>
      <FormattedDate value={timestamp} />
      {breakLines ? <br /> : <>&nbsp;</>}
      <FormattedTime value={timestamp} />
    </>
  )
}

export type { TimestampFormatterProps }
export { TimestampFormatter }
