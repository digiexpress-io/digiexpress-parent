import React from 'react';
import { FormattedMessage } from 'react-intl';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, Box, TextField, Typography } from '@mui/material';

import { HdesApi } from '@dxs-ts/wrench-api';

import Builder, { 
  EditBoolean,
  EditString, EditStringSimple,
  EditNumber, EditNumberSimple,
  EditDateTime, EditDateTimeSimple,
  EditDate, EditDateSimple } from './builders';
import { ValueSetChooser } from './builders/ValueSetChooser';
import { CancelButton } from '@dxs-ts/eveli-primitives';


interface CellEditProps {
  dt:HdesApi.AstDecision,
  cell:HdesApi.AstDecisionCell;
  onClose: () => void;
  onChange: (commands:HdesApi.AstCommand) => void
};

const CellEdit: React.FC<CellEditProps> = (props) => {

  const header:HdesApi.TypeDef = [...props.dt.headers.acceptDefs, ...props.dt.headers.returnDefs]
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

  return (
    <Dialog
      open={true}
      onClose={props.onClose}
      maxWidth="md"
      fullWidth
      scroll="body"
    >
      <DialogTitle>
        <FormattedMessage id='decisions.cells.dialog.title.simple' />
      </DialogTitle>
      <DialogContent sx={{ pt: 2, pb: 1, px: 3 }}>
        <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 2 }}>
          <FormattedMessage
            id='decisions.cells.dialog.title'
            values={{
              name: props.dt.name,
              column: header.name,
              value: props.cell.value ?? <FormattedMessage id="decisions.cells.newvalue.boolean.empty" />
            }}
          />
        </Typography>
        <Box display="flex" flexDirection="column" gap={2}>
          {editor}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button variant='text' onClick={() => {
          const builder = Builder({ header, value: undefined }) as any;
          setValue({ value: undefined, builder });
        }}>
          <FormattedMessage id="decisions.cells.newvalue.clear" />
        </Button>
        <CancelButton onClick={props.onClose} />
        <Button onClick={() => {
          const command:HdesApi.AstCommand = { id: props.cell.id, value: value.value, type: 'SET_CELL_VALUE' };
          props.onChange(command);
          props.onClose();
        }}>
          <FormattedMessage id='buttons.apply' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export type { CellEditProps };
export { CellEdit };
