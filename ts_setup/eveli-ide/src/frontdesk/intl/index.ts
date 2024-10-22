import en from './en';
import fi from './fi';
import sv from './sv';
import anyTaskMessages from './messages.json';


export const frontdeskIntl: { [key: string]: any } = {
  en: { ...anyTaskMessages.en, ...en, },
  fi: { ...anyTaskMessages.fi, ...fi, },
  sv: { ...anyTaskMessages.sv, ...sv, }
};


