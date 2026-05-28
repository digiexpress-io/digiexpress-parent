import React from 'react';
import { TextField, Button, Dialog, DialogTitle, DialogContent, DialogActions, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { FsDirentButtonCancel } from '../../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../../fs-dirent-button-save';

interface CellEditProps {
  dt: Fs.DecisionAst;
  cell: Fs.DecisionAstCell;
  onClose: () => void;
  onChange: (command: Fs.AstCommand) => void;
}

const CellEdit: React.FC<CellEditProps> = ({ dt, cell, onClose, onChange }) => {
  const header = [...dt.headers.acceptDefs, ...dt.headers.returnDefs].find(t => t.id === cell.header);
  const [value, setValue] = React.useState<string | undefined>(cell.value);

  return (
    <Dialog open={true} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <FormattedMessage id='decisions.cells.dialog.title.simple' />
      </DialogTitle>
      <DialogContent sx={{ pt: 2, pb: 1, px: 3 }}>
        <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 2 }}>
          <FormattedMessage
            id='decisions.cells.dialog.title'
            values={{
              name: dt.name,
              column: header?.name ?? '',
              value: cell.value ?? <FormattedMessage id="decisions.cells.newvalue.boolean.empty" />
            }}
          />
        </Typography>
        <TextField
          size="small"
          fullWidth
          value={value ?? ''}
          onChange={(e) => setValue(e.target.value || undefined)}
        />
      </DialogContent>
      <DialogActions>
        <Button variant='text' onClick={() => setValue(undefined)}>
          <FormattedMessage id="decisions.cells.newvalue.clear" />
        </Button>
        <FsDirentButtonCancel onClick={onClose} />
        <FsDirentButtonSave onClick={() => {
          onChange({ id: cell.id, value: value?.trim(), type: 'SET_CELL_VALUE' });
          onClose();
        }} />
      </DialogActions>
    </Dialog>
  );
};

export type { CellEditProps };
export { CellEdit };
