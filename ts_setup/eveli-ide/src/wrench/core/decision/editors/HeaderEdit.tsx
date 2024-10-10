import React from 'react';

import { Box, Grid, ListItemText } from '@mui/material';
import Burger from '@/burger';
import { Client } from '../../context';
import { useIntl } from 'react-intl';
import { EditValueSet } from './builders/EditValueSet';


interface HeaderEditProps {
  dt: Client.AstDecision,
  header: Client.TypeDef;
  onClose: () => void;
  onChange: (commands: Client.AstCommand[]) => void
};

/**
          case 'name': return {type: 'SET_HEADER_REF', id: id, value: change.get('name')}
          case 'ref': return {type: 'SET_HEADER_EXTERNAL_REF', id: id, value: change.get('ref')}
          case 'value': return {type: 'SET_HEADER_TYPE', id: id, value: change.get('value')}
          case 'script': return {type: 'SET_HEADER_SCRIPT', id: id, value: change.get('script')}
          case 'direction': return {type: 'SET_HEADER_DIRECTION', id: id, value: change.get('direction')}
          case 'expression': return {type: 'SET_HEADER_EXPRESSION', id: id, value: change.get('expression')} }
 */

const addCommand = (command: Client.AstCommand, commands: Client.AstCommand[]) => {
  const result: Client.AstCommand[] = [];
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
  const [commands, setCommands] = React.useState<Client.AstCommand[]>([]);
  const [exp, setExp] = React.useState<string>('');
  const [name, setName] = React.useState<string>(header.name);
  const [script, setScript] = React.useState<string>('');
  const [valueType, setValueType] = React.useState<string>(header.valueType);
  const [valueSet, setValueSet] = React.useState<string[]>(header.valueSet ? header.valueSet : []);
  const expressions = dt.headerExpressions[header.valueType] ;
  const intl = useIntl();
  const editor = (
    <Box component="form" noValidate autoComplete="off">
      <Grid container spacing={2}>

        {/** name and type */}
        <Grid item xs={6}>
          <Burger.TextField label={intl.formatMessage({ id: 'dt.header.name' })}
            value={name}
            onChange={(value: string) => {
              setCommands(addCommand({ type: 'SET_HEADER_REF', id: header.id, value }, commands));
              setName(value);
            }} />
        </Grid>
        <Grid item xs={6}>
          <Burger.Select label={intl.formatMessage({ id: 'dt.header.dataType' })}
            selected={valueType}
            onChange={(value) => {
              setCommands(addCommand({ type: 'SET_HEADER_TYPE', id: header.id, value }, commands));
              setValueType(value);
            }}
            empty={{ id: '', label: intl.formatMessage({ id: 'dt.header.dataType' }) }}
            items={dt.headerTypes.map((type) => ({
              id: type,
              value: (<ListItemText primary={type} />)
            }))}
          />
        </Grid>

        <Grid item xs={6}>
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
        </Grid>
      </Grid>
      {header.direction === 'IN' && header.valueType === 'STRING' && <EditValueSet valueSet={valueSet} setValueSet={setValueSet} commands={commands} setCommands={setCommands} headerId={header.id} />}
    </Box >);


  return (<Burger.Dialog open={true}
    onClose={onClose}
    children={editor}
    backgroundColor="uiElements.main"
    title='decisions.header.dialog.title'
    titleArgs={{
      name: dt.name,
      column: header.name
    }}
    actions={
      <Burger.SecondaryButton label={intl.formatMessage({ id: 'dt.header.delete' })} onClick={() => {
        onChange([{ type: 'DELETE_HEADER', id: header.id }]);
        onClose();
      }} />
    }
    submit={{
      title: "buttons.apply",
      disabled: false,
      onClick: () => {
        onChange(commands);
        onClose();
      }
    }}
  />);
}

export type { HeaderEditProps };
export { HeaderEdit };
