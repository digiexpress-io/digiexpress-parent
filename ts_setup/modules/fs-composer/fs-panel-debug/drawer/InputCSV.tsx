import React from 'react'
import { TextareaAutosize, Box, Divider, Chip, Typography, Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { useIntl } from 'react-intl'


interface InputCSVProps {
  value: string;
  selected?: string;
  onClose: () => void;
  onSelect: (csv: string) => void;
}

const InputCSV: React.FC<InputCSVProps> = ({ onSelect, onClose, value }) => {
  const intl = useIntl();
  const [csv, setCsv] = React.useState(value);

  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle>{intl.formatMessage({ id: 'debug.input.csvUpload' })}</DialogTitle>
    <DialogContent>
      <Box>
        <Box><Typography variant="h4" fontWeight="bold">{intl.formatMessage({ id: 'debug.input.csvFileTitle' })}</Typography></Box>
        <input type="file" accept=".csv" onChange={(e) => {
          const file = e.target.files?.[0];
          if (!file) {
            return;
          }
          const reader = new FileReader();
          reader.onload = (ev) => setCsv(ev.target?.result as string ?? '');
          reader.readAsText(file);
        }} />
      </Box>

      <Divider sx={{ mt: 4, mb: 4 }}>
        <Chip label={intl.formatMessage({ id: 'debug.input.csvFileOrText' })} />
      </Divider>

      <TextareaAutosize minRows={10}
        style={{ width: '100%', height: '100%' }}
        value={csv}
        onChange={({ target }) => setCsv(target.value)}
      />
    </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={() => {
            onSelect(csv);
            onClose();
          }}>
          {intl.formatMessage({ id: 'buttons.apply' })}
        </Button>
      </DialogActions>
    </Dialog>);
}

export type { InputCSVProps };
export { InputCSV };
