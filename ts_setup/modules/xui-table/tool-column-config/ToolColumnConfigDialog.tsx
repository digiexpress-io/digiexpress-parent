import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { Table } from '@tanstack/react-table';

import { useIntl } from 'react-intl';
import { ToolColumnConfig } from './ToolColumnConfig';


export interface ToolColumnConfigDialogProps {
  open: boolean,
  table: Table<any>;
  onClose: () => void,
}



export const ToolColumnConfigDialog: React.FC<ToolColumnConfigDialogProps> = ({ open, onClose, table }) => {
  const intl = useIntl();
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth='xs'>
      <DialogTitle>{intl.formatMessage({ id: 'eveli.table.menu.sort.chooseCols', defaultMessage: 'Select columns' })}</DialogTitle>
      <DialogContent>
        <ToolColumnConfig table={table} />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{intl.formatMessage({ id: 'buttons.close', defaultMessage: 'Close' })}</Button>
      </DialogActions>
    </Dialog>)
}
