import React from 'react';
import { useIntl } from 'react-intl';

import { DialobApi } from '../api-dialob';
import { UnknownSlot } from './UnknownSlot';


import { GInputText, GInputTextProps } from '../g-input-text';
import { GInputTextArea, GInputTextAreaProps } from '../g-input-textarea';
import { GInputBoolean, GInputBooleanProps } from '../g-input-boolean';
import { GInputAddress, GInputAddressProps } from '../g-input-address';
import { GFormPage, GFormPageProps } from '../g-form-page';
import { GFormGroup, GFormGroupProps } from '../g-form-group';
import { GFormNote, GFormNoteProps } from '../g-form-note';
import { GInputList, GInputListProps } from '../g-input-list';
import { GInputMultilist, GInputMultilistProps } from '../g-input-multilist';
import { GInputDecimal, GInputDecimalProps } from '../g-input-decimal';
import { GInputDate, GInputDateProps } from '../g-input-date';
import { GInputInt, GInputIntProps } from '../g-input-int';
import { GInputTime, GInputTimeProps } from '../g-input-time';
import { GInputGroup, GInputGroupProps } from '../g-input-group';
import { GInputGroupRow, GInputGroupRowProps } from '../g-input-group-row';
import { GInputSurvey, GInputSurveyProps } from '../g-input-survey';
import { GInputSurveyQuestion, GInputSurveyQuestionProps } from '../g-input-survey-question';
import { LabelPosition } from '../g-input-base';
import { GFormBaseElementProps } from './GFormBaseElement';


export const UNDEFINED_SELECTION_VALUE = '_undefined_';

export type GFormBaseSlot<P> = [React.ElementType<P>, P]

export function useSlot(props: GFormBaseElementProps): GFormBaseSlot<any> {
  const { actionItem: element, formStore: store, onAfterComplete} = props;

  const { variant } = getSlotVariant(element, store);
  const intl = useIntl();

  if (variant === 'date') {
    const errors = store.form.toErrors(element.id);
    const desc = store.form.toDescription(element.id);
    const labelPosition = getLabelPosition(element, store);

    const result: [React.ElementType, GInputDateProps] = [GInputDate, {
      id: element.id,
      label: element.label,
      description: desc,
      errors: errors,
      value: element.value,
      variant: 'date',
      format: undefined,
      onChange: () => {},
      labelPosition
    }];
    return result;
  }

  if (variant === 'time') {
    const errors = store.form.toErrors(element.id);
    const desc = store.form.toDescription(element.id);
    const labelPosition = getLabelPosition(element, store);

    const result: [React.ElementType, GInputTimeProps] = [GInputTime, {
      id: element.id,
      label: element.label,
      description: desc,
      errors: errors,
      value: element.value,
      variant: 'time',
      labelPosition,
      format: undefined,
      onChange: () => {}
    }];
    return result;
  }
  if (variant === 'text') {
    const errors = store.form.toErrors(element.id);
    const desc = store.form.toDescription(element.id);
    const labelPosition = getLabelPosition(element, store);

    const result: [React.ElementType, GInputTextProps] = [GInputText, {
      id: element.id,
      label: element.label,
      description: desc,
      errors: errors,
      value: element.value,
      variant: 'text',
      labelPosition,
      onChange: () => {}
    }];
    return result;
  }

  if (variant === 'text-textBox') {
    const errors = store.form.toErrors(element.id);
    const desc = store.form.toDescription(element.id);
    const labelPosition = getLabelPosition(element, store);

    const result: [React.ElementType, GInputTextAreaProps] = [GInputTextArea, {
      id: element.id,
      label: element.label,
      description: desc,
      errors: errors,
      value: element.value,
      variant: 'textBox',
      onChange: () => {},
      labelPosition
    }];
    return result;
  }

  if (variant === 'text-address') {
    const errors = store.form.toErrors(element.id);
    const desc = store.form.toDescription(element.id);
    const labelPosition = getLabelPosition(element, store);

    const result: [React.ElementType, GInputAddressProps] = [GInputAddress, {
      id: element.id,
      label: element.label,
      description: desc,
      errors: errors,
      value: element.value,
      variant: 'address',
      onChange: () => {},
      labelPosition
    }];
    return result;
  }
  if (variant === 'decimal') {
    const errors = store.form.toErrors(element.id);
    const desc = store.form.toDescription(element.id);
    const labelPosition = getLabelPosition(element, store);

    const result: [React.ElementType, GInputDecimalProps] = [GInputDecimal, {
      id: element.id,
      label: element.label,
      description: desc,
      errors: errors,
      value: element.value,
      variant: 'decimal',
      onChange: () => {},
      labelPosition
    }];
    return result;
  }

  if (variant === 'number') {
    const errors = store.form.toErrors(element.id);
    const desc = store.form.toDescription(element.id);
    const labelPosition = getLabelPosition(element, store);

    const result: [React.ElementType, GInputIntProps] = [GInputInt, {
      id: element.id,
      label: element.label,
      description: desc,
      errors: errors,
      value: element.value,
      variant: 'int',
      onChange: () => {},
      labelPosition
    }];
    return result;
  }

  if (variant === 'page') {
    
    const meta = store.form.toPage(element.id);
    const description = store.form.toDescription(element.id);
    const nextPage = meta.nextPageId ? store.form.getItem(meta.nextPageId) : undefined;

    // there are more page, but the backend is providing one page at a time
    let subTitle: string | undefined;
    if(meta.nextPageId && nextPage) {
      subTitle = intl.formatMessage({ id: 'gamut.forms.page.subtitle'  }, { nextPageTitle: nextPage.label }  );

    // no more more pages
    } else if(!meta.next) {
      subTitle = intl.formatMessage({ id: 'gamut.forms.page.subtitle.complete'  });
    }

    const pages: { id: string; title: string | undefined, pageNumber: number }[] = store.form.pages.map(page => ({
      id: page.id,
      title: store.form.getItem(page.id)?.label,
      pageNumber: page.order
    }));

    function onChangePage(pageId: string) {
      store.goToPage(pageId)
    }

    function onNextPage() {
      store.next();
    }

    function onComplete() {
      store.complete();
      onAfterComplete();
    }
  
    const result: [React.ElementType, GFormPageProps] = [GFormPage, {
      id: element.id,
      title: element.label,
      children: undefined,
      active: meta.active,
      pageNumber: meta.order,

      proceedAllowed: store.form.proceedAllowed,
      completeAllowed: store.form.completeAllowed,

      subTitle,
      description,
      pages,
      onChangePage,
      onNextPage,
      onComplete
    }];
    return result;
  }

  if (variant === 'surveygroup') {
    const errors = store.form.toErrors(element.id);
    const description = store.form.toDescription(element.id);
    const options = store.form.toValueSet(element.id);
    const questions = store.form.toChildren(element.id);
    const vertical = element.view === 'verticalSurveygroup';
    const labelPosition = getLabelPosition(element, store);
    
    const result: [React.ElementType, GInputSurveyProps] = [GInputSurvey, {
      id: element.id,
      label: element.label,
      options: options?.entries.map(e => ({ id: e.key, label: e.value, description: undefined })) ?? [],
      questions: questions.map(e => ({ 
        label: e.label ?? '', 
        description: store.form.toDescription(e.id),
        id: e.id,
        value: e.value as any
      })),
      description,
      children: undefined,
      vertical,
      errors,
      onChange: () => {},
      labelPosition
    }];
    return result;
  }

  if (variant === 'survey') {
  
    const description = store.form.toDescription(element.id);
    const parent = store.form.toParent(element.id)
    const options = parent ? store.form.toValueSet(parent.id) : undefined;
    const questions = parent ? store.form.toChildren(parent?.id) : [];
    const index = questions.map(item => item.id).indexOf(element.id);


    const result: [React.ElementType, GInputSurveyQuestionProps] = [GInputSurveyQuestion, {
      id: element.id,
      label: element.label,
      description,
      index,
      value: element.value,
      options: options?.entries.map(e => ({ id: e.key, label: e.value, description: undefined })) ?? [],
      onChange: () => {}
      
    }];
    return result;
  }

  if (variant === 'group') {
    const desc = store.form.toDescription(element.id);
    const result: [React.ElementType, GFormGroupProps] = [GFormGroup, {
      id: element.id,
      label: element.label,
      description: desc,
      children: undefined,
      columns: element.props?.columns 
    }];
    return result;
  }

  if (variant === 'rowgroup') {
    const description = store.form.toDescription(element.id);
    const result: [React.ElementType, GInputGroupProps] = [GInputGroup, {
      id: element.id,
      label: element.label,
      description,
      children: undefined,
      onAddRow: (id: string) => store.addRowToGroup(id)
    }];
    return result;
  }

  if (variant === 'row') {

    const meta = store.form.toInputRow(element.id);
    const description = store.form.toDescription(element.id);
    const result: [React.ElementType, GInputGroupRowProps] = [GInputGroupRow, {
      id: element.id,
      label: element.label,
      description,
      children: undefined,
      order: meta.order,
      total: meta.total,
      columns: element.props?.columns,
      onDelete: (id: string) => store.deleteRow(id)
    }];
    return result;
  }

  if (variant === 'boolean') {
    const errors = store.form.toErrors(element.id);
    const desc = store.form.toDescription(element.id);
    const labelPosition = getLabelPosition(element, store);
    const result: [React.ElementType, GInputBooleanProps] = [GInputBoolean, {
      id: element.id,
      label: element.label,
      description: desc,
      variant: 'checkbox',
      errors,
      value: element.value,
      onChange: () => {},
      labelPosition
    }];
    return result;
  }


  if (variant === 'list') {
    const valueset = store.form.toValueSet(element.id);
    const desc = store.form.toDescription(element.id);
    const labelPosition = getLabelPosition(element, store);
    const errors = store.form.toErrors(element.id);
    const result: [React.ElementType, GInputListProps] = [GInputList, {
      id: element.id,
      label: element.label,
      description: desc,
      errors,
      variant: 'list',
      undefinedValue: UNDEFINED_SELECTION_VALUE,
      value: element.value ?? UNDEFINED_SELECTION_VALUE,
      datasource: valueset!,
      onChange: () => {},
      labelPosition
    }];
    return result;
  }


  if (variant === 'multichoice') {
    const valueset = store.form.toValueSet(element.id);
    const desc = store.form.toDescription(element.id);
    const labelPosition = getLabelPosition(element, store);
    const errors = store.form.toErrors(element.id);
    const result: [React.ElementType, GInputMultilistProps] = [GInputMultilist, {
      id: element.id,
      label: element.label,
      description: desc,
      variant: 'multilist',
      errors,
      value: element.value,
      datasource: valueset!,
      onChange: () => {},
      labelPosition
    }];
    return result;
  }


  if (variant === 'note') {
    const result: [React.ElementType, GFormNoteProps] = [GFormNote, {
      id: element.id,
      label: element.label
    }];
    return result;
  }

  return [UnknownSlot, { id: element.id, element, variant }]
}




interface GFormBaseSlots {
  'text': React.ElementType<GInputTextProps>;
  'text-textBox': React.ElementType<GInputTextAreaProps>;
  'text-address': React.ElementType<GInputTextProps>;

  'decimal': React.ElementType<GInputDecimalProps>;
  'number': React.ElementType<GInputIntProps>;
  'page': React.ElementType<GFormPageProps>;
  'group': React.ElementType<GFormPageProps>;
  
  'rowgroup': React.ElementType<GInputGroupProps>;
  'row': React.ElementType<GInputGroupRowProps>;

  'surveygroup': React.ElementType<GInputSurveyProps>;
  'survey': React.ElementType<GInputSurveyQuestionProps>;

  'boolean': React.ElementType<GInputBooleanProps>;
  'list': React.ElementType<GInputListProps>;
  'note': React.ElementType<GFormNoteProps>;
  'date': React.ElementType<GInputDateProps>;
  'time': React.ElementType<GInputTimeProps>;
  'multichoice': React.ElementType<GInputMultilistProps>;
}


interface SlotVariant {
  variant: keyof GFormBaseSlots
}


function getLabelPosition(element: DialobApi.ActionItem, store: DialobApi.FormStore): LabelPosition {
  try {
    const parent = store.form.toParent(element.id);
    return parseInt(parent?.props.columns) > 1 ? 'label-top': 'label-left';
  } catch(e) {
    return 'label-left';
  }
}

function getSlotVariant(element: DialobApi.ActionItem, store: DialobApi.FormStore): SlotVariant {
  if(element.type === 'group' && !element.view && store.form.pagesIds.includes(element.id)) {
    return { variant: 'page' };
  }
  if(element.type === 'group' && element.view === 'page') {
    return { variant: 'page' };
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
