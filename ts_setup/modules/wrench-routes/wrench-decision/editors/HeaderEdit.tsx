import React from 'react';

import { Box, Grid2, ListItemText, Button, Dialog, DialogTitle, DialogContent, DialogActions, Typography } from '@mui/material';
import * as Burger from '@dxs-ts/eveli-primitives';
import { HdesApi } from '@dxs-ts/wrench-api';
import { FormattedMessage, useIntl } from 'react-intl';
import { EditValueSet } from './builders/EditValueSet';
import { EditIntlValueSet } from './builders';
import { CancelButton } from '@dxs-ts/eveli-primitives';


interface HeaderEditProps {
  dt:HdesApi.AstDecision,
  header:HdesApi.TypeDef;
  onClose: () => void;
  onChange: (commands:HdesApi.AstCommand[]) => void
};

/**
          case 'name': return {type: 'SET_HEADER_REF', id: id, value: change.get('name')}
          case 'ref': return {type: 'SET_HEADER_EXTERNAL_REF', id: id, value: change.get('ref')}
          case 'value': return {type: 'SET_HEADER_TYPE', id: id, value: change.get('value')}
          case 'script': return {type: 'SET_HEADER_SCRIPT', id: id, value: change.get('script')}
          case 'direction': return {type: 'SET_HEADER_DIRECTION', id: id, value: change.get('direction')}
          case 'expression': return {type: 'SET_HEADER_EXPRESSION', id: id, value: change.get('expression')} }
 */

const addCommand = (command:HdesApi.AstCommand, commands:HdesApi.AstCommand[]) => {
  const result:HdesApi.AstCommand[] = [];
  for (const previous of commands) {
    if (command.type === previous.type) {
      
    } else {
      result.push(previous);
    }
  }
  result.push(command);
  return result;
}

const HeaderEdit: React.FC<HeaderEditProps> = ({ dt, header, onClose, onChange }) => {
  const [commands, setCommands] = React.useState<HdesApi.AstCommand[]>([]);
  const [exp, setExp] = React.useState<string>('');
  const [name, setName] = React.useState<string>(header.name);
  const [script, setScript] = React.useState<string>('');
  const [valueType, setValueType] = React.useState<string>(header.valueType);
  const [valueSet, setValueSet] = React.useState<string[]>(header.valueSet ? header.valueSet : []);
  const expressions = dt.headerExpressions[header.valueType] ;
  const intl = useIntl();

  const editor = (
    <Box component="form" noValidate autoComplete="off">
      <Grid2 container spacing={2}>

        {/** name and type */}
        <Grid2 size={{ xs: 12 }}>
          <Burger.TextField label={intl.formatMessage({ id: 'dt.header.name' })}
            value={name}
            onChange={(value: string) => {
              setCommands(addCommand({ type: 'SET_HEADER_REF', id: header.id, value }, commands));
              setName(value);
            }} />
        </Grid2>

        <Grid2 size={{ xs: 12 }}>
          <Burger.Select label={intl.formatMessage({ id: 'dt.header.dataType' })}
            selected={valueType}
            onChange={(value) => {
              setCommands(addCommand({ type: 'SET_HEADER_TYPE', id: header.id, value }, commands));
              setValueType(value);
            }}
            empty={{ id: '', label: intl.formatMessage({ id: 'dt.header.dataType' }) }}
            items={dt.headerTypes
              .filter(type => {
                if(header.direction === 'IN' && type === 'INTL') {
                  return false;
                }
                return true;
              })
              .map((type) => ({
                id: type,
                value: (<ListItemText primary={type} />)
              }))}
          />
        </Grid2>
        <Grid2 size={{ xs: 12 }}>
          {valueType === 'INTL' && <EditIntlValueSet valueSet={valueSet} 
            setValueSet={setValueSet} 
            commands={commands} 
            setCommands={setCommands} 
            headerId={header.id} />}
        </Grid2>

        <Grid2 size={{ xs: 12 }}>
          {header.direction === 'OUT' ? null : (
            <Burger.Select label={intl.formatMessage({ id: 'dt.header.expression' })}
              selected={exp}
              onChange={(value) => {
                setCommands(addCommand({ type: 'SET_HEADER_EXPRESSION', id: header.id, value }, commands));
                setExp(value);
              }}
              empty={{ id: '', label: intl.formatMessage({ id: 'dt.header.expression' }) }}
              items={(expressions ? expressions : []).map((type) => ({
                id: type,
                value: (<ListItemText primary={type} />)
              }))} />)}

          {header.direction === 'IN' ? null : (
            <Burger.Select label={intl.formatMessage({ id: 'dt.header.script' })}
              selected={script}
              onChange={(value) => {
                setCommands(addCommand({ type: 'SET_HEADER_SCRIPT', id: header.id, value }, commands));
                setScript(value);
              }}
              empty={{ id: '', label: intl.formatMessage({ id: 'dt.header.script' }) }}
              items={(expressions ? expressions : []).map((type) => ({
                id: type,
                value: (<ListItemText primary={type} />)
              }))} />)}
        </Grid2>
        {valueType === 'STRING' && <Grid2 size={{ xs: 12 }}>
          <EditValueSet valueSet={valueSet} setValueSet={setValueSet} commands={commands} setCommands={setCommands} headerId={header.id} />
        </Grid2>}
      </Grid2>
    </Box >);

  return (<Dialog open={true} onClose={onClose}>
    <DialogTitle>
      <FormattedMessage id='decisions.header.dialog.title.simple' />
    </DialogTitle>
    <DialogContent>
      <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 2 }}>
        <FormattedMessage
          id='decisions.header.dialog.title'
          values={{ name: dt.name, column: header.name }}
        />
      </Typography>
      {editor}
    </DialogContent>
    <DialogActions>
      <Button variant='text' children={intl.formatMessage({ id: 'dt.header.delete' })} onClick={() => {
        onChange([{ type: 'DELETE_HEADER', id: header.id }]);
        onClose();
      }} />
      <CancelButton onClick={onClose} />
      <Button onClick={() => {
          onChange(commands);
          onClose(); }}>
        <FormattedMessage id='buttons.apply'/>
      </Button>
    </DialogActions>
  </Dialog>);
}

export type { HeaderEditProps };
export { HeaderEdit };
