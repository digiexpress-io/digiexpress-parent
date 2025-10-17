import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GFormNote } from './GFormNote';



export const GFormNoteDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store, navRef, navRefId }) => {
  const style = element.props?.style;

  return (
    <>
      <div ref={navRef} id={navRefId} />
      <GFormNote
        id={element.id}
        label={store.form.toLabel(element.id)}
        description={element.description}
        style={style}
      />
    </>
  );
}
