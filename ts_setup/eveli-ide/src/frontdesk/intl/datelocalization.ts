import { Locale } from 'date-fns';
import { enUS } from 'date-fns/locale/en-US';
import { fi } from 'date-fns/locale/fi';
import { sv } from 'date-fns/locale/sv';

export const DATE_LOCALE_MAP: {[key: string]: Locale} = {
  en: enUS,
  fi: fi,
  sv: sv
};