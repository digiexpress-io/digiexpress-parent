
import { DialobApi } from '../api-dialob';


export const UNDEFINED_SELECTION_VALUE = '_undefined_';


export type GFormBaseSlotVariant = (
  'text' |
  'text-fileUpload' | 
  'text-textBox'|
  'text-address' |

  'decimal' |
  'number' |
  'page' |
  'group' |
  
  'rowgroup' |  
  'row' |

  'surveygroup' |
  'survey' |

  'boolean' |
  'list' |
  'note' |
  'date' |
  'time' |
  'multichoice'
)

export interface SlotVariant {
  variant: GFormBaseSlotVariant
}
export function useSlotVariant(element: DialobApi.ActionItem, store: DialobApi.FormStore): SlotVariant {
  if(element.type === 'group' && !element.view && store.form.pagesIds.includes(element.id)) {
    return { variant: 'page' };
  }
  if(element.type === 'group' && element.view === 'page') {
    return { variant: 'page' };
  }
  if(element.type === 'text' && element.view === 'text' && element.props?.controlType === 'fileUpload') {
    return { variant: 'text-fileUpload' };
  }
  if(element.type === 'text' && element.view === 'text') {
    return { variant: 'text' };
  }
  if(element.type === 'surveygroup' && (
    !element.view || 
    element.view === 'verticalSurveygroup' || 
    element.view === 'horizontalSurveygroup' )) {
    return { variant: 'surveygroup' };
  }
  if(element.type === 'survey' && element.view === 'survey') {
    return { variant: 'survey' };
  }
  const variant = element?.type + (element.view ? '-' + element.view : '') as any;
  return { variant };
}
