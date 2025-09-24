import { DateTime } from 'luxon';

/**
 * Formats day names using Luxon
 * Returns array of 2-letter day abbreviations starting from Sunday
 * @param locale - Locale code (e.g., 'en-US', 'fr-FR', 'de-DE')
 * @returns Array of 7 day names (2-letter format)
 */
function formatDayName(locale: string): Record<string, string> {
  const startDate = DateTime.fromObject({ year: 1970, month: 1, day: 4 }); // Sunday, Jan 4, 1970
  const dayNames: Record<string, string> = {};
  
  for (let i = 0; i < 7; i++) {
    const day = startDate.plus({ days: i });
    const formatted = day.setLocale(locale).toFormat('ccc'); // 3-letter format first
    dayNames['calendar.day.' + i] = formatted.substring(0, 2);
  }
  return dayNames;
}

/**
 * Formats month names using Luxon
 * Returns array of full month names
 * @param locale - Locale code (e.g., 'en-US', 'fr-FR', 'de-DE')
 * @returns Array of 12 full month names
 */
function formatMonthName(locale: string): Record<string, string> {
  const startDate = DateTime.fromObject({ year: 1970, month: 1, day: 1 }); // Jan 1, 1970
  const monthNames: Record<string, string> = {};
  
  for (let i = 0; i < 12; i++) {
    const month = startDate.plus({ months: i });
    const formatted = month.setLocale(locale).toFormat('MMMM'); // Full month name
    monthNames['calendar.month.' + i] = capitalize(formatted);
  }
  
  return monthNames;
}

function capitalize(s: string){
    return String(s[0]).toUpperCase() + String(s).slice(1);
}

export function getMessages(locale: string): Record<string, string> {
  return {
    ...formatMonthName(locale),
    ...formatDayName(locale)
  }
}