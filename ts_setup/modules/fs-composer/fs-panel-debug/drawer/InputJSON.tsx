import React from 'react'
import { TextareaAutosize, Box, Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { useIntl } from 'react-intl'

interface InputJSONProps {
  value: string;

  onClose: () => void;
  onSelect: (json: object) => void;
}

const parseInput = (value: string) => {
  var parsed = JSON.parse(value);
  for (var key in parsed) {
    if (parsed[key].includes(" - ")) {
      parsed[key] = parsed[key].split(" - ")[0];
    }
    if (parsed[key].includes(", ")) {
      parsed[key] = parsed[key].split(", ")[0];
    }
  }
  var stringified = JSON.stringify(parsed, null, 2);
  return stringified;
}

const InputJSON: React.FC<InputJSONProps> = ({ onSelect, onClose, value }) => {
  const intl = useIntl();
  const [json, setJson] = React.useState<string>(parseInput(value));


  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'debug.input.json' })}</DialogTitle>
      <DialogContent>
        <Box>
          <Box><Typography variant="h4" fontWeight="bold">{intl.formatMessage({ id: 'debug.input.jsonTitle' })}</Typography></Box>
        </Box>
        <TextareaAutosize minRows={10}
          style={{ width: '100%', height: '100%' }}
          value={json}
          onChange={({ target }) => setJson(target.value)}
        />
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={() => {
          try {
            onSelect(JSON.parse(json));
            onClose();
          } catch (e) {
            console.error(e);
          }
        }}>
          {intl.formatMessage({ id: 'buttons.apply' })}
        </Button>
      </DialogActions>
    </Dialog>);
}

export type { InputJSONProps };
export { InputJSON };
