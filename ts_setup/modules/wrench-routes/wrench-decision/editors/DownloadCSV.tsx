import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, Select, MenuItem, Typography, Box, TextField } from '@mui/material';
import { HdesApi } from '@dxs-ts/wrench-api';
import { FormattedMessage, useIntl } from 'react-intl';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import FileSaver from 'file-saver';



interface DownloadCSVProps {
  onClose: () => void;
  decision: HdesApi.AstDecision;
}

export const saveCsv = (decision: HdesApi.AstDecision, delimiter: string) => {
  const accepts: HdesApi.TypeDef[] = [...decision.headers.acceptDefs].sort((a, b) => a.order - b.order);
  const returns: HdesApi.TypeDef[] = [...decision.headers.returnDefs].sort((a, b) => a.order - b.order);
  const rows = decision.rows.sort((a, b) => a.order - b.order);
  const headers: HdesApi.TypeDef[] = [...accepts, ...returns];

  const line0 = headers.map(h => h.name).join(delimiter);
  const lines = rows.map(row => {
    const cells: Record<string, HdesApi.AstDecisionCell> = {};
    row.cells.forEach(e => cells[e.header] = e);
    return headers
      .map(header => cells[header.id])
      .map(c => `${c.value ? c.value : ''}`)
      .join(delimiter);
  }).join("\r\n");
  

	FileSaver.saveAs(line0 + "\r\n" + lines, decision.name + '.csv');
}

const DownloadCSV: React.FC<DownloadCSVProps> = ({ decision, onClose }) => {
  const intl = useIntl();
  const [delimiter, setDelimiter] = React.useState(',');
  const [customDelimiter, setCustomDelimiter] = React.useState('');

  const handleDownload = () => {
    const finalDelimiter = delimiter === 'custom' ? customDelimiter : delimiter;
    if (!finalDelimiter) {
      return;
    }
    saveCsv(decision, finalDelimiter);
    onClose();
  };

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='decisions.toolbar.csvDownload' /></DialogTitle>
      <DialogContent>
        <Typography sx={{ mb: 1 }}>
          <FormattedMessage id='decisions.csv.delimiter' defaultMessage='Delimiter' />
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
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDownload} disabled={delimiter === 'custom' && !customDelimiter}>
          <FormattedMessage id='buttons.download' defaultMessage='Download' />
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export type { DownloadCSVProps };
export { DownloadCSV };

