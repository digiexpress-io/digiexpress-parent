import React from 'react';

import { Box, Typography, List, Button } from '@mui/material';
import ChecklistIcon from '@mui/icons-material/Checklist';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';

import type { ChecklistItem } from './ChecklistItem';
import ChecklistRow from './ChecklistItem';
import Styles from '@styles';
import ChecklistItemDialog from './ChecklistItemDialog';

const ChecklistHeader: React.FC = () => {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', ml: 3, mb: 1 }}>
      <ChecklistIcon color='primary' sx={{ mr: 3 }} />
      <Typography variant="h4" fontWeight='h1.fontWeight'>Checklist</Typography>
    </Box>
  );
}

const demoChecklistItems: ChecklistItem[] = [
  {
    id: 'CHECK_1',
    text: 'Item 1',
    completed: true,
    dueDate: '07/25/2023'
  },
  {
    id: 'CHECK_2',
    text: 'Item 2',
    completed: false,
    dueDate: '07/25/2023',
    assignees: ['John Doe', 'Jane Doe'],
  },
  {
    id: 'CHECK_3',
    text: 'Item 3',
    completed: false,
  },
];

const Checklist: React.FC = () => {
  const [tempChecklistItems, setTempChecklistItems] = React.useState<ChecklistItem[]>(demoChecklistItems);
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

  const handleItemClick = (item: ChecklistItem) => {
    setActiveItem(item);
    setMode('edit');
    setOpen(true);
  };

  const handleUpdate = (item: ChecklistItem) => {
    console.log(item)
    const newChecklistItems = [...tempChecklistItems];
    const index = newChecklistItems.findIndex((i) => i.id === item.id);
    newChecklistItems[index] = item;
    console.log(newChecklistItems)
    setTempChecklistItems(newChecklistItems);
  };

  return (
    <Box>
      <ChecklistHeader />
      <Styles.ProgressBar progress={calculateProgress()} />
      <List>
        {tempChecklistItems.map((item, index) => <ChecklistRow key={item.id} item={item} onChecked={() => handleChecked(index)} onDeleteClick={() => handleDeleted(index)} onClick={handleItemClick} />)}
      </List>
      <Button variant='outlined' startIcon={<AddCircleOutlineIcon />} sx={{ m: 1, ml: 3 }} onClick={() => setOpen(true)}>Add Item</Button>
      <ChecklistItemDialog mode={mode} open={open} onClose={() => setOpen(false)} onSave={handleAdd} onUpdate={handleUpdate} item={activeItem} />
    </Box>
  );
}

export { Checklist };
