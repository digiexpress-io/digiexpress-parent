import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GFormGroup } from './GFormGroup';
import { DialobApi } from '@dxs-ts/gamut-api';



export const GFormGroupDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store, disabled, children }) => {
  const desc = store.form.toDescription(element.id);

  // starts from 1: Page is group 1
  const parents = store.form.toParents(element.id);
  const border: boolean | undefined = element.props?.border ? (DialobApi.isTrue(element.props?.border)) : undefined;
  const collapsible: boolean | undefined = element.props?.collapsible ? (DialobApi.isTrue(element.props?.collapsible)) : undefined;

  return (
    <GFormGroup
      id={element.id}
      label={store.form.toLabel(element.id)}
      description={desc}
      disabled={disabled}
      children={children}
      level={parents.length}
      border={border}
      collapsible={collapsible}
      columns={element.props?.columns}
      readOnly={element.readOnly}
    />);
}


