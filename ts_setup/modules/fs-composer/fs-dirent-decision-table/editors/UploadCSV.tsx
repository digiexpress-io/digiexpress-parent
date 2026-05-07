import React from 'react';
import { TextareaAutosize, Button, Dialog, DialogTitle, DialogContent, DialogActions, Select, MenuItem, Box, Typography, TextField } from '@mui/material';
import { Fs } from '@dxs-ts/fs-api';

import { FormattedMessage, useIntl } from 'react-intl';

interface UploadCSVProps {
  onClose: () => void;
  onChange: (commands: Fs.AstCommand[]) => void;
}

const UploadCSV: React.FC<UploadCSVProps> = ({ onChange, onClose }) => {
  const intl = useIntl();
  const [csv, setCsv] = React.useState('');
  const [delimiter, setDelimiter] = React.useState(',');
  const [customDelimiter, setCustomDelimiter] = React.useState('');

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='decisions.toolbar.csvUpload' /></DialogTitle>
      <DialogContent>
        <Box sx={{ mb: 2, mt: 1 }}>
          <Typography sx={{ mb: 1 }}>
            <FormattedMessage id='decisions.csv.delimiter' />
          </Typography>
          <Select fullWidth value={delimiter} onChange={(e) => setDelimiter(e.target.value)}>
            <MenuItem value=",">,</MenuItem>
            <MenuItem value=";">;</MenuItem>
            <MenuItem value="custom"><FormattedMessage id='decisions.csv.delimiter.custom' /></MenuItem>
          </Select>
          {delimiter === 'custom' && (
            <TextField fullWidth sx={{ mt: 2 }}
              placeholder={intl.formatMessage({ id: 'decisions.csv.delimiter.custom.placeholder' })}
              value={customDelimiter}
              onChange={(e) => setCustomDelimiter(e.target.value)}
            />
          )}
        </Box>
        <TextareaAutosize minRows={15} style={{ width: '100%' }} value={csv} onChange={({ target }) => setCsv(target.value)} />
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={() => {
          const commands: Fs.AstCommand[] = [];
          if (csv.trim().length > 0) {
            commands.push({ type: 'IMPORT_ORDERED_CSV', value: csv, id: delimiter === 'custom' ? customDelimiter : delimiter });
          }
          if (commands.length > 0) {
            onChange(commands);
          }
          onClose();
        }}
          disabled={delimiter === 'custom' && !customDelimiter}
        >
          <FormattedMessage id='buttons.apply' />
        </Button>
      </DialogActions>
    </Dialog>);
}

export type { UploadCSVProps };
export { UploadCSV };
