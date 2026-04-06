import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputGroupRow } from './GInputGroupRow';



export const GInputGroupRowDialob: React.FC<GFormBaseElementProps> = ({ disabled, actionItem: element, formStore: store, children }) => {
  const meta = store.form.toInputRow(element.id);
  const description = store.form.toDescription(element.id);

  return (
    <GInputGroupRow
      id={element.id}
      disabled={disabled}
      label={store.form.toLabel(element.id)}
      description={description}
      children={children}
      order={meta.order}
      total={meta.total}
      columns={element.props?.columns}
      onDelete={(id: string) => store.deleteRow(id)}
      readOnly={element.readOnly}
    />);
}
