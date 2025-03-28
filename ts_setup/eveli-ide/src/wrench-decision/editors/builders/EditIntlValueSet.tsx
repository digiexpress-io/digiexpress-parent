import React from 'react'
import { InputLabel, List, ListItem, ListItemButton, ListItemText, ListItemIcon, Divider, Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl'

import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import * as Burger from '@/eveli-styles';
import { HdesApi } from '@/api-wrench';

export interface EditIntlValueSetProps {
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


export const EditIntlValueSet: React.FC<EditIntlValueSetProps> = ({ valueSet, setValueSet, commands, setCommands, headerId }) => {

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
      <ListItemButton onClick={() => handleRemoveValue(index)}>
        <ListItemIcon>
          <DeleteOutlineIcon color='error' />
        </ListItemIcon>
        <Typography fontWeight='bold'>{value}</Typography>
      </ListItemButton>
    </ListItem>
  ));


  return (
    <>
      <Burger.TextField
        label='decisions.addLocale.addNew'
        value={value}
        onChange={setValue}
        onEnter={() => {
          handleAddValue(value.toLocaleLowerCase())
          setValue('')
        }} />
      <InputLabel sx={{ mt: 1 }}><FormattedMessage id='decisions.addLocale.current' /></InputLabel>
      <List>{list}</List>
    </>
  );

}
