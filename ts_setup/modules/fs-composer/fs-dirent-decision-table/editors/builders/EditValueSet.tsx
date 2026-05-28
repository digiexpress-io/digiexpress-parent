import React from 'react';
import { TextField, List, ListItem, ListItemButton, ListItemText, ListItemIcon, Box, Typography } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import { DeleteOutline as DeleteOutlineIcon } from '@mui/icons-material';
import { Fs } from '@dxs-ts/fs-api';

interface EditValueSetProps {
  valueSet: string[];
  setValueSet: (valueSet: string[]) => void;
  commands: Fs.AstCommand[];
  setCommands: (commands: Fs.AstCommand[]) => void;
  headerId: string;
}

const addCommand = (command: Fs.AstCommand, commands: Fs.AstCommand[]) => {
  const result: Fs.AstCommand[] = [];
  for (const previous of commands) {
    if (command.type !== previous.type) {
      result.push(previous);
    }
  }
  result.push(command);
  return result;
};

export const EditValueSet: React.FC<EditValueSetProps> = ({ valueSet, setValueSet, commands, setCommands, headerId }) => {
  const intl = useIntl();
  const [value, setValue] = React.useState<string>('');

  const handleAddValue = (val: string) => {
    if (val) {
      const newValueSet = [...valueSet, val];
      setValueSet(newValueSet);
      setCommands(addCommand({ type: 'SET_VALUE_SET', id: headerId, value: newValueSet.join(', ') }, commands));
    }
  };

  const handleRemoveValue = (id: number) => {
    const newValueSet = valueSet.filter((_, index) => index !== id);
    setValueSet(newValueSet);
    setCommands(addCommand({ type: 'SET_VALUE_SET', id: headerId, value: newValueSet.join(', ') }, commands));
  };

  const list = valueSet.map((v, index) => (
    <ListItem disablePadding key={index}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <ListItemButton onClick={() => handleRemoveValue(index)} sx={{ justifyContent: 'center' }}>
          <ListItemIcon sx={{ minWidth: 'auto' }}>
            <DeleteOutlineIcon />
          </ListItemIcon>
        </ListItemButton>
        <ListItemText primary={v} />
      </Box>
    </ListItem>
  ));

  return (
    <>
      <TextField
        size="small"
        fullWidth
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder={intl.formatMessage({ id: 'decisions.valueSet.add.placeholder' })}
        onKeyDown={(e) => {
          if (e.key === 'Enter') {
            handleAddValue(value);
            setValue('');
          }
        }}
      />
      <Typography sx={{ mt: 2 }}><FormattedMessage id='decisions.valueSet.current' />:</Typography>
      {valueSet.length > 0
        ? <List>{list}</List>
        : <Typography><FormattedMessage id='decisions.valueSet.current.empty' /></Typography>}
    </>
  );
};
