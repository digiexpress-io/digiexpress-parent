import en from './en';
import { messages as stencil } from 'components-stencil';
import { messages as hdes } from 'components-hdes/core';


const result: {[key: string]: any}  = {
  en: {...en, ...stencil['en'], ...hdes['en']},

};

export default result;
