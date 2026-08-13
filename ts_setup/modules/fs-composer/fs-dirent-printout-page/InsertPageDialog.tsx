import React from 'react';
import { Dialog, DialogContent, DialogTitle, List, ListItemButton, ListItemText, TextField } from '@mui/material';
import { FsDirentPrintoutPageDialogList, useUtilityClasses } from './useUtilityClasses';
import { FsIcons } from '../fs-theme';
import { PageOption } from './useUpdateOwnerState';



export const InsertPageDialog: React.FC<{
  open: boolean;
  currentTemplateIds: string[];
  pages: PageOption[];
  onSelect: (page: PageOption) => void;
  onClose: () => void;
}> = ({ open, currentTemplateIds, pages, onSelect, onClose }) => {
  const classes = useUtilityClasses();
  const [filter, setFilter] = React.useState('');

  const filtered = filter ? pages.filter(p => p.templateName.toLowerCase().includes(filter.toLowerCase())) : pages;

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Insert printout page</DialogTitle>
      <DialogContent>
        <TextField
          autoFocus
          fullWidth
          placeholder='Search...'
          value={filter}
          onChange={e => setFilter(e.target.value)}
          size='small'
        />
        <FsDirentPrintoutPageDialogList>
          <List>
            {filtered.map(page => (
              <ListItemButton key={page.id} className={classes.dialogListItem} onClick={() => onSelect(page)}>
                <ListItemText primary={page.templateName} />
                <div className={classes.dialogItemEnd}>
                  {currentTemplateIds.includes(page.id) && (
                    <FsIcons.Checkmark className={classes.dialogCheckmark} />
                  )}
                </div>
              </ListItemButton>
            ))}
          </List>
        </FsDirentPrintoutPageDialogList>
      </DialogContent>
    </Dialog>
  );
};