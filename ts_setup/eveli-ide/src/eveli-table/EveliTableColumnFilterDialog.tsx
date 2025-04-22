import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { Table } from '@tanstack/react-table';
import { ColSelectItem, EveliTableColSelect } from './EveliTableColSelect';


interface EveliTableColumnFilterProps {
  open: boolean,
  onClose: () => void,
  table: Table<any>;
}

export const EveliTableColumnFilterDialog: React.FC<EveliTableColumnFilterProps> = ({ open, onClose, table }) => {
  const allColumns = table.getAllColumns().filter(col => col.getCanHide());

  return (
    <Dialog open={open} onClose={onClose} maxWidth='xs'>
      <DialogTitle>Select columns</DialogTitle>
      <DialogContent>
        <EveliTableColSelect>
          {allColumns.map((col, index) => (<ColSelectItem colTitle={col.columnDef.header?.toString() || col.id} key={index}
            isVisible={col.getIsVisible()}
            onToggle={() => col.toggleVisibility()} />
          )
          )}
        </EveliTableColSelect>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>)
}