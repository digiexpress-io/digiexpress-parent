import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GFormNote } from './GFormNote';



export const GFormNoteDialob: React.FC<GFormBaseElementProps> = ({ actionItem: element, formStore: store, navRef, navRefId }) => {
  const style = element.props?.style;
  const labelPosition = store.form.toLabelPosition(element.id);

  return (
    <>
      <div ref={navRef} id={navRefId} />
      <GFormNote
        id={element.id}
        label={store.form.toLabel(element.id)}
        description={element.description}
        labelPosition={labelPosition}
        style={style}
      />
    </>
  );
}
