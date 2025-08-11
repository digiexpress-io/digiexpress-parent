import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GFormGroup } from './GFormGroup';



export const GFormGroupDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store, children }) => {
  const desc = store.form.toDescription(element.id);

  // starts from 1: Page is group 1
  const parents = store.form.toParents(element.id);
  const border: boolean | undefined = element.props?.border ? (element.props?.border === 'true') : undefined;
  const collapsible: boolean | undefined = element.props.collapsible ? (element.props.collapsible === 'true') : undefined;

  return (
    <GFormGroup
      id={element.id}
      label={element.label}
      description={desc}
      children={children}
      level={parents.length}
      border={border}
      collapsible={collapsible}
      columns={element.props?.columns}
    />);
}


