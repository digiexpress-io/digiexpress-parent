import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GFormGroup } from './GFormGroup';



export const GFormGroupDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store, children }) => {
  const desc = store.form.toDescription(element.id);

  return (
    <GFormGroup
      id={element.id}
      label={element.label}
      description={desc}
      children={children}
      columns={element.props?.columns}
    />);
}


