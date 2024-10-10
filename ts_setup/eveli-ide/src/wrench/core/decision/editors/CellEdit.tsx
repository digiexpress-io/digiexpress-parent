import React from 'react';

import Burger from '@/burger';
import { Client } from '../../context';

import Builder, { 
  EditBoolean,
  EditString, EditStringSimple,
  EditNumber, EditNumberSimple,
  EditDateTime, EditDateTimeSimple,
  EditDate, EditDateSimple } from './builders';
import { ValueSetChooser } from './builders/ValueSetChooser';


interface CellEditProps {
  dt: Client.AstDecision,
  cell: Client.AstDecisionCell;
  onClose: () => void;
  onChange: (commands: Client.AstCommand) => void
};

const CellEdit: React.FC<CellEditProps> = (props) => {

  const header: Client.TypeDef = [...props.dt.headers.acceptDefs, ...props.dt.headers.returnDefs]
    .filter(t => t.id === props.cell.header)[0];

  const [value, setValue] = React.useState<{ value?: string, builder: any }>({
    value: props.cell.value,
    builder: Builder({ header, value: props.cell.value }) as any
  });
  const input = header.direction === 'IN'
  const type = header.valueType;

  const handleChangeValue = (value: string) => {
    const builder = Builder({ header, value }) as any;
    setValue({ value, builder });
  }

  let editor: React.ReactElement;
  if (type === 'STRING') {
    editor = header.valueSet && header.valueSet.length !== 0 ? 
      <ValueSetChooser builder={value.builder} valueSet={header.valueSet} onChange={handleChangeValue} /> :
      (input ?
      <EditString builder={value.builder} onChange={handleChangeValue} /> :
      <EditStringSimple builder={value.builder} onChange={handleChangeValue} />)

  } else if (type === 'INTEGER' || type === 'LONG' || type === 'DECIMAL') {
    editor = input ?
      <EditNumber builder={value.builder} onChange={handleChangeValue} /> :
      <EditNumberSimple builder={value.builder} onChange={handleChangeValue} />

  } else if (type === 'DATE') {
    editor = input ?
      <EditDate builder={value.builder} onChange={handleChangeValue} /> :
      <EditDateSimple builder={value.builder} onChange={handleChangeValue} />

  } else if (type === 'DATE_TIME') {
    editor = input ?
      <EditDateTime builder={value.builder} onChange={handleChangeValue} /> :
      <EditDateTimeSimple builder={value.builder} onChange={handleChangeValue} />

  } else if (type === 'BOOLEAN') {
    editor = <EditBoolean builder={value.builder as any} onChange={handleChangeValue} />
  } else {
    editor = (<></>);
  }

  return (<Burger.Dialog open={true}
    onClose={props.onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title='decisions.cells.dialog.title'
    titleArgs={{
      name: props.dt.name,
      column: header.name,
      value: props.cell.value
    }}
    actions={
      <Burger.SecondaryButton label="decisions.cells.newvalue.clear" onClick={() => {
        const builder = Builder({ header, value: undefined }) as any;
        setValue({ value: undefined, builder });
      }} />
    }
    submit={{
      title: "buttons.apply",
      disabled: false,
      onClick: () => {
        const command: Client.AstCommand = { id: props.cell.id, value: value.value, type: 'SET_CELL_VALUE' };
        props.onChange(command);
        props.onClose();
      }
    }}
  />);
}

export type { CellEditProps };
export { CellEdit };
