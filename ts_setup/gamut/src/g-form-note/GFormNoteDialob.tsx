import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GFormNote } from './GFormNote';



export const GFormNoteDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store }) => {
  return (
    <GFormNote
      id={element.id}
      label={element.label}
    />);
}
