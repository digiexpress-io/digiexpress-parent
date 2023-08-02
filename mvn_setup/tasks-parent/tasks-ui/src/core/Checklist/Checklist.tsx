import React from 'react';

import { Box, Typography, List, Button, IconButton } from '@mui/material';
import ChecklistIcon from '@mui/icons-material/Checklist';
import DeleteIcon from '@mui/icons-material/Delete';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';

import ChecklistItemComponent from './ChecklistItem';
import ChecklistItemDialog from './ChecklistItemDialog';
import { Checklist, ChecklistItem } from 'taskclient/task-types';
import Styles from '@styles';

const ChecklistHeader: React.FC<{ title: string }> = ({ title }) => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', ml: 3, mb: 1 }}>
      <Box sx={{ display: 'flex', alignItems: 'center' }}>
        <ChecklistIcon color='primary' sx={{ mr: 3 }} />
        <Typography variant="h4" fontWeight='h1.fontWeight'>{title}</Typography>
      </Box>
      <IconButton color='error'><DeleteIcon /></IconButton>
    </Box>
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
    console.log(item)
    const newChecklistItems = [...tempChecklistItems];
    const index = newChecklistItems.findIndex((i) => i.id === item.id);
    newChecklistItems[index] = item;
    console.log(newChecklistItems)
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
