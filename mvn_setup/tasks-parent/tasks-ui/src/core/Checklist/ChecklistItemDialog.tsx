import React from 'react';

import { Dialog, Box, TextField, Button, Typography } from '@mui/material';
import { ChecklistItem, ChecklistItemActions } from './ChecklistItem';

interface ChecklistItemDialogProps {
  mode: 'add' | 'edit';
  open: boolean;
  onClose: () => void;
  item?: ChecklistItem;
  onDeleteClick?: () => void;
  onSave: (item: ChecklistItem) => void;
  onUpdate: (item: ChecklistItem) => void;
}

const ChecklistItemDialog: React.FC<ChecklistItemDialogProps> = (props) => {
  const { mode, open, onClose, item, onDeleteClick, onSave, onUpdate } = props;
  const { id, text, completed, dueDate, assignees } = item || {};

  const [tempText, setTempText] = React.useState<string>('');
  const [tempDueDate, setTempDueDate] = React.useState<Date | string | undefined>();
  const [tempAssignees, setTempAssignees] = React.useState<string[]>([]);

  React.useEffect(() => {
    setTempText(text || '');
    setTempDueDate(dueDate);
    setTempAssignees(assignees || []);
  }, [text, dueDate, assignees]);

  const handleSaveClick = () => {
    const newItem: ChecklistItem = {
      id: id || Math.random().toString(),
      text: tempText,
      completed: completed || false,
      dueDate: tempDueDate?.toLocaleString(),
      assignees: tempAssignees,
    };
    mode === 'add' ? onSave(newItem) : onUpdate(newItem);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <Box sx={{ p: 4, width: '500px' }}>
        <Typography variant='h4' sx={{ mb: 2 }}>Add a checklist item</Typography>
        <TextField fullWidth value={tempText} onChange={(e) => setTempText(e.target.value)} />
        <Box sx={{ mt: 2, display: 'flex', alignItems: 'center' }}>
          <Button variant='contained' color='primary' sx={{ mr: 1 }} onClick={handleSaveClick}>Save</Button>
          <Button variant='outlined' color='primary' onClick={onClose}>Cancel</Button>
          <Box flexGrow={1} />
          <ChecklistItemActions mode={mode} dueDate={tempDueDate} setDueDate={setTempDueDate} assignees={tempAssignees} setAssignees={setTempAssignees} onDeleteClick={onDeleteClick} />
        </Box>
      </Box>
    </Dialog>
  );
}

export default ChecklistItemDialog;