import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { ColSelectItem, EveliTableColSelect } from './EveliTableColSelect';


export const EveliTableColumnFilterDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ open, onClose }) => {

  return (
    <Dialog open={open} onClose={onClose} maxWidth='xs'>
      <DialogTitle>Select columns</DialogTitle>
      <DialogContent>
        <EveliTableColSelect>
          <ColSelectItem colTitle='Priority' />
          <ColSelectItem colTitle='Name' />
          <ColSelectItem colTitle='Client' />
          <ColSelectItem colTitle='Status' />
          <ColSelectItem colTitle='Assignee' />
          <ColSelectItem colTitle='Info' />
          <ColSelectItem colTitle='Due' />
          <ColSelectItem colTitle='Created' />
        </EveliTableColSelect>
      </DialogContent>
      <DialogActions><Button onClick={onClose}>Close</Button></DialogActions>
    </Dialog>)
}