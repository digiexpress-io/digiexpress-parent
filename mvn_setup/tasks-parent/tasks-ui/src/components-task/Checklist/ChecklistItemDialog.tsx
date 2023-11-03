import React from 'react';

import { Dialog, Box, TextField, Button, Typography } from '@mui/material';

import DatePicker from '../DatePicker';
import AssigneePicker from './AssigneePicker';
import { ChecklistItemActions } from './ChecklistItem';
import { ChecklistItemDialogProps } from './checklist-types';
import { ChecklistItem } from 'client/task-types';

const demoAssignees: string[] = ['John Doe', 'Jane Doe', 'John Smith', 'Jane Smith'];

const ChecklistItemDialog: React.FC<ChecklistItemDialogProps> = (props) => {
  const { mode, open, onClose, item, onDeleteClick, onSave, onUpdate } = props;
  const { id, title, completed, dueDate, assigneeIds } = item || {};

  const [tempText, setTempText] = React.useState<string>('');
  const [tempDueDate, setTempDueDate] = React.useState<Date | string | undefined>();
  const [tempAssignees, setTempAssignees] = React.useState<string[]>([]);

  const [datePickerOpen, setDatePickerOpen] = React.useState<boolean>(false);
  const [assigneePickerOpen, setAssigneePickerOpen] = React.useState<boolean>(false);

  React.useEffect(() => {
    if (mode === 'edit') {
      setTempText(title || '');
      setTempDueDate(dueDate);
      setTempAssignees(assigneeIds || []);
    }
    if (mode === 'add') {
      setTempText('');
      setTempDueDate(undefined);
      setTempAssignees([]);
    }
  }, [title, dueDate, assigneeIds, mode]);

  const handleSaveClick = () => {
    const newItem: ChecklistItem = {
      id: id || Math.random().toString(),
      title: tempText,
      completed: completed || false,
      dueDate: new Date(tempDueDate || '07/07/2023').toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' }),
      assigneeIds: tempAssignees,
    };
    mode === 'add' ? onSave(newItem) : onUpdate(newItem);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <Box sx={{ p: 4, width: '500px' }}>
        <Typography variant='h4' sx={{ mb: 2 }}>{mode === 'add' ? 'Add' : 'Edit'} a checklist item</Typography>
        <TextField fullWidth value={tempText} onChange={(e) => setTempText(e.target.value)} />
        <Box sx={{ mt: 2, display: 'flex', alignItems: 'center' }}>
          <Button variant='contained' color='primary' sx={{ mr: 1 }} onClick={handleSaveClick}>Save</Button>
          <Button variant='text' color='primary' onClick={onClose}>Cancel</Button>
          <Box flexGrow={1} />
          <ChecklistItemActions
            mode={mode}
            dueDate={tempDueDate}
            assigneeIds={tempAssignees}
            onDeleteClick={onDeleteClick}
            setDatePickerOpen={setDatePickerOpen}
            setAssigneePickerOpen={setAssigneePickerOpen}
          />
        </Box>
      </Box>
      <Dialog open={datePickerOpen} onClose={() => setDatePickerOpen(false)}>
        <DatePicker dueDate={tempDueDate} setDueDate={setTempDueDate} onClose={() => setDatePickerOpen(false)} />
      </Dialog>
      <AssigneePicker possible={demoAssignees} chosen={tempAssignees} setChosen={setTempAssignees} open={assigneePickerOpen} onClose={() => setAssigneePickerOpen(false)} />
    </Dialog>
  );
}

export default ChecklistItemDialog;