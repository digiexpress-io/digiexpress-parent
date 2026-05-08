import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, Select, MenuItem, Typography, TextField } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import fileDownload from 'js-file-download';

interface DownloadCSVProps {
  decision: Fs.DecisionAst;
  onClose: () => void;
}

export const saveCsv = (decision: Fs.DecisionAst, delimiter: string) => {
  const accepts: Fs.DecisionTypeDef[] = [...decision.headers.acceptDefs].sort((a, b) => a.order - b.order);
  const returns: Fs.DecisionTypeDef[] = [...decision.headers.returnDefs].sort((a, b) => a.order - b.order);
  const rows = decision.rows.sort((a, b) => a.order - b.order);
  const headers: Fs.DecisionTypeDef[] = [...accepts, ...returns];

  const line0 = headers.map(h => h.name).join(delimiter);
  const lines = rows.map(row => {
    const cells: Record<string, Fs.DecisionAstCell> = {};
    row.cells.forEach(e => cells[e.header] = e);
    return headers
      .map(header => cells[header.id])
      .map(c => `${c.value ? c.value : ''}`)
      .join(delimiter);
  }).join("\r\n");

  fileDownload(line0 + "\r\n" + lines, decision.name + '.csv');
};

const DownloadCSV: React.FC<DownloadCSVProps> = ({ decision, onClose }) => {
  const intl = useIntl();
  const [delimiter, setDelimiter] = React.useState(',');
  const [customDelimiter, setCustomDelimiter] = React.useState('');

  const handleDownload = () => {
    const finalDelimiter = delimiter === 'custom' ? customDelimiter : delimiter;
    if (!finalDelimiter) return;
    saveCsv(decision, finalDelimiter);
    onClose();
  };

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'decisions.toolbar.csvDownload' })}</DialogTitle>
      <DialogContent>
        <Typography sx={{ mb: 1 }}>
          {intl.formatMessage({ id: 'decisions.csv.delimiter' })}
        </Typography>
        <Select fullWidth value={delimiter} onChange={(e) => setDelimiter(e.target.value)}>
          <MenuItem value=",">,</MenuItem>
          <MenuItem value=";">;</MenuItem>
          <MenuItem value="custom">{intl.formatMessage({ id: 'decisions.csv.delimiter.custom' })}</MenuItem>
        </Select>
        {delimiter === 'custom' && (
          <TextField fullWidth sx={{ mt: 2 }}
            placeholder={intl.formatMessage({ id: 'decisions.csv.delimiter.custom.placeholder' })}
            value={customDelimiter}
            onChange={(e) => setCustomDelimiter(e.target.value)}
          />
        )}
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={handleDownload} disabled={delimiter === 'custom' && !customDelimiter}>
          {intl.formatMessage({ id: 'buttons.download' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export type { DownloadCSVProps };
export { DownloadCSV };
