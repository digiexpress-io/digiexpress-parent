import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { Table, VisibilityState } from '@tanstack/react-table';
import { ColSelectItem, EveliTableColumnSelect } from './EveliTableColumnSelect';
import { useIntl } from 'react-intl';


interface EveliTableColumnVisibilityDialogProps {
  open: boolean,
  onClose: () => void,
  table: Table<any>;
}



export const EveliTableColumnVisibilityDialog: React.FC<EveliTableColumnVisibilityDialogProps> = ({ open, onClose, table }) => {
  const allColumns = table.getAllColumns().filter(col => col.getCanHide());
  const intl = useIntl();

  const [selected, setSelected] = React.useState<VisibilityState>(allColumns
    .reduce<Record<string, boolean>>((coll, next) => {
      coll[next.id] = next.getIsVisible();
      return coll;
    }, {}));

  function handleColumnVisibility(id: string) {
    setSelected(prev => {
      const next = {...prev};
      next[id] = !prev[id];
      return next
    });
  }


  function handleClose() {
    table.setColumnVisibility(prev => ({...prev, ...selected}));
    onClose();
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth='xs'>
      <DialogTitle>{intl.formatMessage({ id: 'eveli.table.menu.sort.chooseCols', defaultMessage: 'Select columns' })}</DialogTitle>
      <DialogContent>
        <EveliTableColumnSelect>
          {allColumns.map((col, index) => (<ColSelectItem colTitle={col.columnDef.header?.toString() || col.id} key={index}
            isVisible={selected[col.id]}
            onToggle={() => handleColumnVisibility(col.id)} />
          )
          )}
        </EveliTableColumnSelect>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Close</Button>
      </DialogActions>
    </Dialog>)
}
