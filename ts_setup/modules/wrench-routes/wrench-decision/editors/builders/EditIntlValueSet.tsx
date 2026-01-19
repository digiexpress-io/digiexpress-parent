import React from 'react';
import { InputLabel, List, ListItem, IconButton, Box, Button, Typography } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import { DeleteOutline as DeleteOutlineIcon } from '@mui/icons-material';
import * as Burger from '@dxs-ts/eveli-primitives';
import { HdesApi } from '@dxs-ts/wrench-api';

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
};

const isValidLocale = (value: string) => /^[a-z]{2}(-[A-Z]{2})?$/.test(value);

export const EditIntlValueSet: React.FC<EditIntlValueSetProps> = ({ valueSet, setValueSet, commands, setCommands, headerId }) => {
  const intl = useIntl();
  const [value, setValue] = React.useState<string>('');
  const trimmed = value.trim();

  const normalized = React.useMemo(() => {
    const parts = trimmed.split('-').filter(Boolean);
    if (!parts.length) return '';
    if (parts.length === 1) return parts[0].toLowerCase();
    return `${parts[0].toLowerCase()}-${parts[1].toUpperCase()}`;
  }, [trimmed]);

  const exists = normalized ? valueSet.includes(normalized) : false;
  const valid = normalized ? isValidLocale(normalized) : true;

  const commitValueSet = (newValueSet: string[]) => {
    setValueSet(newValueSet);
    setCommands(addCommand({ type: 'SET_VALUE_SET', id: headerId, value: newValueSet.join(', ') }, commands));
  };

  const handleAdd = () => {
    if (!normalized || !valid || exists) return;
    commitValueSet([...valueSet, normalized]);
    setValue('');
  };

  const handleRemove = (id: number) => {
    const newValueSet = valueSet.filter((_, index) => index !== id);
    commitValueSet(newValueSet);
  };

  return (
    <Box sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1, p: 2, width: '100%' }} > 
      <Box>
        <Burger.TextField
          label={intl.formatMessage({ id: 'decisions.intl.locales.addNew' })}
          value={value}
          onChange={setValue}
          onEnter={handleAdd}
          helperText={intl.formatMessage({ id: 'decisions.intl.locales.hint' })}
        />
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 0.5 }} >
          <Button variant="contained" onClick={handleAdd} disabled={!normalized || !valid || exists} >
            <FormattedMessage id="button.add" />
          </Button>
        </Box>
      </Box>

      <Box>
        <InputLabel sx={{ mb: 1, fontWeight: 600, marginLeft: 2 }}>
          <FormattedMessage id="decisions.intl.locales.current" />
        </InputLabel>

        <List dense>
          {(valueSet ?? []).map((locale, index) => (
            <ListItem
              key={`${locale}-${index}`}
              secondaryAction={
                <IconButton
                  edge="end"
                  onClick={() => handleRemove(index)}
                  aria-label="delete"
                >
                  <DeleteOutlineIcon color="error" />
                </IconButton>
              }
            >
              <Typography fontWeight="bold">
                {locale}
              </Typography>
            </ListItem>
          ))}
        </List>
      </Box>
    </Box>

  );
};
