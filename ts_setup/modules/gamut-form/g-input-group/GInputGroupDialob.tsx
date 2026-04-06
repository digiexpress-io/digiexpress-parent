import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputGroup } from './GInputGroup';
import { DialobApi } from '@dxs-ts/gamut-api';



export const GInputGroupDialob: React.FC<GFormBaseElementProps> = ({ disabled, actionItem: element, formStore: store, children }) => {
  const description = store.form.toDescription(element.id);
  const border: boolean | undefined = element.props?.border ? (DialobApi.isTrue(element.props?.border)) : undefined;

  return (
    <GInputGroup
      disabled={disabled}
      id={element.id}
      label={store.form.toLabel(element.id)}
      description={description}
      children={children}
      onAddRow={(id: string) => store.addRowToGroup(id)}
      border={border}
      readOnly={element.readOnly}
    />);
}
