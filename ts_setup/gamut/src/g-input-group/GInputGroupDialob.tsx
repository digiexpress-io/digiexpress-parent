import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputGroup } from './GInputGroup';



export const GInputGroupDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store, children }) => {
  const description = store.form.toDescription(element.id);
  const border: boolean | undefined = element.props?.border ? (element.props?.border === 'true') : undefined;

  return (
    <GInputGroup
      id={element.id}
      label={element.label}
      description={description}
      children={children}
      onAddRow={(id: string) => store.addRowToGroup(id)}
      border={border}
    />);
}
