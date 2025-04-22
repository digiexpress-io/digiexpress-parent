import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { ColumnDef } from '@tanstack/react-table';
import { ColSelectItem, EveliTableColSelect } from './EveliTableColSelect';

import { TaskApi } from '@/api-task';


interface EveliTableColumnFilterProps {
  open: boolean,
  onClose: () => void,
  columns: ColumnDef<TaskApi.Task, any>[];
}

export const EveliTableColumnFilterDialog: React.FC<EveliTableColumnFilterProps> = ({ open, onClose, columns }) => {

  return (
    <Dialog open={open} onClose={onClose} maxWidth='xs'>
      <DialogTitle>Select columns</DialogTitle>
      <DialogContent>
        <EveliTableColSelect>
          {columns.map((col, index) => (
            <ColSelectItem colTitle={col.header?.toString() ? col.header.toString() : 'none'} key={index} />)
          )}
        </EveliTableColSelect>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>)
}