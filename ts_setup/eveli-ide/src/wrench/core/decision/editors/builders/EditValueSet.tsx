import React from 'react'
import { InputLabel, List, ListItem, ListItemButton, ListItemText, ListItemIcon, Divider, Box } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import Burger from '@/burger';
import { Client } from '../../../context';

interface EditValueSetProps {
  valueSet: string[];
  setValueSet: (valueSet: string[]) => void;
  commands: Client.AstCommand[];
  setCommands: (commands: Client.AstCommand[]) => void;
  headerId: string;
}

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


export const EditValueSet: React.FC<EditValueSetProps> = ({ valueSet, setValueSet, commands, setCommands, headerId }) => {

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

  const list = valueSet && valueSet.length > 0 && valueSet.map((value, index) => (
    <ListItem disablePadding key={index}>
      <Box display="flex">
        <Box flexGrow={0}>
          <ListItemButton onClick={() => handleRemoveValue(index)}>
            <ListItemIcon>
              <DeleteOutlineIcon />
            </ListItemIcon>
          </ListItemButton>
        </Box>
        <Box flexGrow={1}>
          <ListItemText primary={value} />
        </Box>
      </Box>
    </ListItem>
  ));


  return (
    <>
      <Divider sx={{ mt: 2 }}/>
      <InputLabel sx={{ mt: 1, fontWeight: 'bold', color: '#000' }}><FormattedMessage id='decisions.valueSet' /></InputLabel>
      <InputLabel sx={{ mt: 1 }}><FormattedMessage id='decisions.valueSet.description' /></InputLabel>
      <Burger.TextField
        label='decisions.valueSet.add'
        value={value}
        onChange={setValue}
        onEnter={() => handleAddValue(value)} />
      <InputLabel sx={{ mt: 1 }}><FormattedMessage id='decisions.valueSet.current' /></InputLabel>
      <List>{list}</List>
    </>
  );

}
