import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GFormNoteValidation } from './GFormNoteValidation';



export const GFormNoteValidationDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store, navRef, navRefId }) => {

  const style = element.props?.style;

  return (
    <>
      <div ref={navRef} id={navRefId} />
      <GFormNoteValidation
        id={element.id}
        label={store.form.toLabel(element.id)}
        style={style}
        description={store.form.toDescription(element.id)}
      />
    </>
  );
}
