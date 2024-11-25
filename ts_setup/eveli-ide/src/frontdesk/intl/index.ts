import en from './en';
import fi from './fi';
import sv from './sv';
import { messages } from './messages';


export const frontdeskIntl: { [key: string]: any } = {
  en: { ...messages.en, ...en, },
  fi: { ...messages.fi, ...fi, },
  sv: { ...messages.sv, ...sv, }
};


