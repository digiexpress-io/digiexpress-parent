import en from './en';
import { messages as stencil } from 'components-stencil';


const result: {[key: string]: any}  = {
  en: {...en, ...stencil['en']},

};

export default result;
