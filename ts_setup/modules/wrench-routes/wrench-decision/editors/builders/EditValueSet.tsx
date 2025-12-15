import React from 'react'
import { List, ListItem, ListItemButton, ListItemText, ListItemIcon, Box, Typography } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl'

import { DeleteOutline as DeleteOutlineIcon } from '@mui/icons-material';
import * as Burger from '@dxs-ts/eveli-primitives';
import { HdesApi } from '@dxs-ts/wrench-api';

interface EditValueSetProps {
  valueSet: string[];
  setValueSet: (valueSet: string[]) => void;
  commands: HdesApi.AstCommand[];
  setCommands: (commands: HdesApi.AstCommand[]) => void;
  headerId: string;
}

const addCommand = (command: HdesApi.AstCommand, commands: HdesApi.AstCommand[]) => {
  const result: HdesApi.AstCommand[] = [];
  for (const previous of commands) {
    if (command.type === previous.type) {
      
    } else {
      result.push(previous);
    }
  }
  result.push(command);
  return result;
}


export const EditValueSet: React.FC<EditValueSetProps> = ({ valueSet, setValueSet, commands, setCommands, headerId }) => {
  const intl = useIntl();
  const [value, setValue] = React.useState<string>('');

  const handleAddValue = (value?: string) => {
    if (value) {
      const newValueSet = [...valueSet, value];
      setValueSet(newValueSet);
      setCommands(addCommand({ type: 'SET_VALUE_SET', id: headerId, value: newValueSet.join(", ") }, commands));
    }
  }

  const handleRemoveValue = (id: number) => {
    const newValueSet = valueSet.filter((_, index) => index !== id);
    setValueSet(newValueSet);
    setCommands(addCommand({ type: 'SET_VALUE_SET', id: headerId, value: newValueSet.join(", ") }, commands));
  }

  const list = valueSet.map((value, index) => (
    <ListItem disablePadding key={index}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <ListItemButton onClick={() => handleRemoveValue(index)} sx={{ justifyContent: 'center' }}>
          <ListItemIcon sx={{ minWidth: 'auto' }}>
            <DeleteOutlineIcon />
          </ListItemIcon>
        </ListItemButton>
        <ListItemText primary={value} />
      </Box>
    </ListItem>
  ));


  return (
    <>
      <Burger.TextField
        label='decisions.valueSet.add'
        value={value}
        onChange={setValue}
        placeholder={intl.formatMessage({ id: 'decisions.valueSet.add.placeholder' })}
        onEnter={() => {
          handleAddValue(value)
          setValue('')
        }} />
      <Typography sx={{ mt: 2 }}><FormattedMessage id='decisions.valueSet.current' />:</Typography>
      {valueSet.length > 0 ? <List>{list}</List> : <Typography><FormattedMessage id='decisions.valueSet.current.empty' /></Typography>}
    </>
  );

}
