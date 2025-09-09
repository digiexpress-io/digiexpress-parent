import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { Table, VisibilityState } from '@tanstack/react-table';

import { useIntl } from 'react-intl';
import { ToolColumnVisibility } from './ToolColumnVisibility';


export interface ToolColumnVisibilityDialogProps {
  open: boolean,
  onClose: () => void,
  table: Table<any>;
}



export const ToolColumnVisibilityDialog: React.FC<ToolColumnVisibilityDialogProps> = ({ open, onClose, table }) => {
  const allColumns = table.getAllColumns().filter(col => col.getCanHide());
  const intl = useIntl();

  const [selected, setSelected] = React.useState<VisibilityState>({});

  React.useEffect(() => {
    if (open) {
      const current = table.getAllColumns().filter(col => col.getCanHide()).reduce<Record<string, boolean>>((coll, next) => {
        coll[next.id] = next.getIsVisible();
        return coll;
      }, {});
      setSelected(current);
    }
  }, [open, table]);

  function handleColumnVisibility(id: string) {
    setSelected(prev => {
      const next = { ...prev };
      next[id] = !prev[id];
      return next
    });
  }

  function handleClose() {
    table.setColumnVisibility(prev => ({ ...prev, ...selected }));
    onClose();
  }


  return (
    <Dialog open={open} onClose={onClose} maxWidth='xs'>
      <DialogTitle>{intl.formatMessage({ id: 'eveli.table.menu.sort.chooseCols', defaultMessage: 'Select columns' })}</DialogTitle>
      <DialogContent>
        <ToolColumnVisibility
          table={table}
          slotProps={{
          columns: allColumns.map((col) => ({
            colTitle: col.columnDef.header?.toString() || col.id,
            isVisible: selected[col.id],
            onToggle: () => handleColumnVisibility(col.id)
          }))
        }} />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>{intl.formatMessage({ id: 'buttons.close', defaultMessage: 'Close' })}</Button>
      </DialogActions>
    </Dialog>)
}
