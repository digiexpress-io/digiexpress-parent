import { DateTime } from 'luxon';

export function formatAnyDateShort(value: Date | string | undefined): string {
  if (!value) return '--';
  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
}

export const FormatAnyDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const rawDate = value;

  if (!rawDate) {
    return <div>--</div>
  }
  const jsDate = new Date(value);
  const dateTime = DateTime.fromJSDate(jsDate).setLocale("fi");
  const formatted = dateTime.toLocaleString(DateTime.DATE_SHORT);

  return <div>{formatted}</div>;
}
