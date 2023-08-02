import React from 'react';

import { Box, Typography, List, Button, IconButton, styled, Dialog, DialogTitle, DialogContent, TextField, DialogActions } from '@mui/material';
import ChecklistIcon from '@mui/icons-material/Checklist';
import DeleteIcon from '@mui/icons-material/Delete';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';

import ChecklistItemComponent from './ChecklistItem';
import ChecklistItemDialog from './ChecklistItemDialog';
import { Checklist, ChecklistItem } from 'taskclient/task-types';
import Styles from '@styles';

const ChecklistHeaderContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'space-between',
  marginLeft: theme.spacing(3),
  marginBottom: theme.spacing(1),
}));

const StyledChecklistTitle = styled(Typography)(({ theme }) => ({
  fontSize: theme.typography.h4.fontSize,
  fontWeight: theme.typography.h1.fontWeight,
  ':hover': {
    cursor: 'pointer',
    textShadow: '2px 2px lightgray',
  },
}));

const ChecklistHeader: React.FC<{ title: string }> = ({ title }) => {
  const [open, setOpen] = React.useState<boolean>(false);
  const [tempTitle, setTempTitle] = React.useState<string>(title);

  const handleCancel = () => {
    setOpen(false);
    setTempTitle(title);
  }

  const handleClose = () => {
    setOpen(false);
  }

  return (
    <ChecklistHeaderContainer>
      <Box sx={{ display: 'flex', alignItems: 'center' }}>
        <ChecklistIcon color='primary' sx={{ mr: 3 }} />
        <StyledChecklistTitle onClick={() => setOpen(true)}>{tempTitle}</StyledChecklistTitle>
      </Box>
      <IconButton color='error'><DeleteIcon /></IconButton>
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>
          <Typography variant='h4'>Edit checklist title</Typography>
        </DialogTitle>
        <DialogContent>
          <TextField fullWidth value={tempTitle} onChange={(e) => setTempTitle(e.target.value)} />
        </DialogContent>
        <DialogActions sx={{ display: 'flex', justifyContent: 'flex-start', pl: 3 }}>
          <Button variant='contained' onClick={handleClose}>Save</Button>
          <Button onClick={handleCancel}>Cancel</Button>
        </DialogActions>
      </Dialog>
    </ChecklistHeaderContainer>
  );
}

const ChecklistComponent: React.FC<{ checklist: Checklist }> = ({ checklist }) => {
  const { title, items } = checklist;
  const [tempChecklistItems, setTempChecklistItems] = React.useState<ChecklistItem[]>(items);
  const [open, setOpen] = React.useState<boolean>(false);
  const [mode, setMode] = React.useState<'add' | 'edit'>('add');
  const [activeItem, setActiveItem] = React.useState<ChecklistItem | undefined>(undefined);

  const calculateProgress = () => {
    const total = tempChecklistItems.length;
    const completed = tempChecklistItems.filter((item) => item.completed).length;
    return (completed / total) * 100;
  };

  const handleChecked = (index: number) => {
    const newChecklistItems = [...tempChecklistItems];
    newChecklistItems[index].completed = !newChecklistItems[index].completed;
    setTempChecklistItems(newChecklistItems);
  };

  const handleDeleted = (index: number) => {
    const newChecklistItems = [...tempChecklistItems];
    newChecklistItems.splice(index, 1);
    setTempChecklistItems(newChecklistItems);
  };

  const handleAdd = (item: ChecklistItem) => {
    const newChecklistItems = [...tempChecklistItems];
    newChecklistItems.push(item);
    setTempChecklistItems(newChecklistItems);
  };

  const handleItemClick = (item: ChecklistItem, e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    setActiveItem(item);
    setMode('edit');
    setOpen(true);
    e.stopPropagation();
  };

  const handleUpdate = (item: ChecklistItem) => {
    const newChecklistItems = [...tempChecklistItems];
    const index = newChecklistItems.findIndex((i) => i.id === item.id);
    newChecklistItems[index] = item;
    setTempChecklistItems(newChecklistItems);
  };

  const handleAddClick = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    setMode('add');
    setOpen(true);
    e.stopPropagation();
  }

  return (
    <Box>
      <ChecklistHeader title={title} />
      <Styles.ProgressBar progress={calculateProgress()} />
      <List>
        {tempChecklistItems.map((item, index) => <ChecklistItemComponent key={item.id} item={item} onChecked={() => handleChecked(index)} onDeleteClick={() => handleDeleted(index)} onClick={handleItemClick} />)}
      </List>
      <Button variant='outlined' startIcon={<AddCircleOutlineIcon />} sx={{ m: 1, ml: 3 }} onClick={(e) => handleAddClick(e)}>Add Item</Button>
      <ChecklistItemDialog mode={mode} open={open} onSave={handleAdd} onUpdate={handleUpdate} item={activeItem} onClose={() => setOpen(false)} />
    </Box>
  );
}

export { ChecklistComponent };
