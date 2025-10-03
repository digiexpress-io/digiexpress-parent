import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GFormNoteValidation } from './GFormNoteValidation';



export const GFormNoteValidationDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store }) => {

  const style = element.props.style;

  return (
    <GFormNoteValidation
      id={element.id}
      label={element.label}
      style={style}
    />);
}
