import React from 'react';
import { useDefaultProps } from '@mui/material/DefaultPropsProvider';

import { DialobApi } from '../api-dialob'
import { UnknownSlot } from './UnknownSlot';

import { GInputUploadDialob } from '../g-input-upload';
import { GInputTextDialob } from '../g-input-text';
import { GInputTextAreaDialob } from '../g-input-textarea';
import { GInputBooleanDialob} from '../g-input-boolean';
import { GInputAddressDialob } from '../g-input-address';
import { GFormPageDialob } from '../g-form-page';
import { GFormGroupDialob } from '../g-form-group';
import { GFormNoteDialob } from '../g-form-note';
import { GInputListDialob } from '../g-input-list';
import { GInputMultilistDialob } from '../g-input-multilist';
import { GInputDecimalDialob } from '../g-input-decimal';
import { GInputDateDialob } from '../g-input-date';
import { GInputIntDialob } from '../g-input-int';
import { GInputTimeDialob } from '../g-input-time';
import { GInputGroupDialob } from '../g-input-group';
import { GInputGroupRowDialob } from '../g-input-group-row';
import { GInputSurveyDialob } from '../g-input-survey';
import { GInputSurveyQuestionDialob } from '../g-input-survey-question';
import { GFormBaseSlotVariant, useSlotVariant } from './useSlotVariant';


export interface GFormBaseElementClasses {
  root: string;
  variant: string;
}
export type GFormBaseElementClassKey = keyof GFormBaseElementClasses;

export interface GFormBaseElementProps {
  actionItem: DialobApi.ActionItem;
  form: DialobApi.Form;
  disabled: boolean;
  formStore: DialobApi.FormStore;
  children?: React.ReactNode | undefined; 
  onAfterComplete: () => void;
}

const MUI_NAME = 'GFormBaseElement';

const Slots: Record<GFormBaseSlotVariant, React.ElementType<GFormBaseElementProps>> =  {
  'date': GInputDateDialob,
  'time': GInputTimeDialob,
  'text': GInputTextDialob,
  'text-fileUpload': GInputUploadDialob,
  'text-textBox': GInputTextAreaDialob,
  'text-address': GInputAddressDialob,
  'decimal': GInputDecimalDialob,
  'number': GInputIntDialob,
  'page': GFormPageDialob,
  'surveygroup': GInputSurveyDialob,
  'survey': GInputSurveyQuestionDialob,
  'group': GFormGroupDialob,
  'rowgroup': GInputGroupDialob,
  'row': GInputGroupRowDialob,
  'boolean': GInputBooleanDialob,
  'list': GInputListDialob,
  'multichoice': GInputMultilistDialob,
  'note': GFormNoteDialob  
}

export const GFormBaseElement: React.FC<GFormBaseElementProps> = (initProps) => {
  const props = useDefaultProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { actionItem: element, formStore: store} = props;
  const { variant } = useSlotVariant(element, store);

  const Component: React.ElementType<GFormBaseElementProps> = Slots[variant] ?? UnknownSlot;
  return (<Component {...props} disabled={props.disabled}>{props.children}</Component>);
}