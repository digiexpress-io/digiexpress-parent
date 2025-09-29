import React from 'react';
import { GFormBaseElementProps, UNDEFINED_SELECTION_VALUE } from '../g-form-base-element';
import { GInputList } from './GInputList';
import { useIntl } from 'react-intl';


/**
 *  Composer property
 *  - `variant = radio` = Flat list of choices with Radio buttons
 *  - `variant = autocomplete` = Drop down single choice menu with autocompleting text input
 *  - defaults to `variant = autocomplete`
 */

export const GInputListDialob: React.FC<GFormBaseElementProps> = ({ disabled, actionItem: element, formStore: store }) => {
  const intl = useIntl();
  const valueset = store.form.toValueSet(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);
  const errors = store.form.toErrors(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const newValue = event.target.value === UNDEFINED_SELECTION_VALUE ? undefined : event.target.value;
    store.setAnswer(element.id, newValue);
  }
  //  const variant = element.props?.variant === 'radio' ? 'list-radio' : 'list';

  const variant = element.props?.variant ?? 'autocomplete';

  return (
    <GInputList
      id={element.id}
      disabled={disabled}
      label={element.label}
      description={desc}
      required={!!element.required}
      errors={errors}
      variant={variant}
      undefinedValue={intl.formatMessage({ id: UNDEFINED_SELECTION_VALUE })}
      value={element.value}
      datasource={valueset}
      onChange={onChange}
      labelPosition={labelPosition}
    />);
}
