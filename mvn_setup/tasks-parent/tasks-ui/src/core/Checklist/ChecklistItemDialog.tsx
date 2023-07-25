import React from 'react';

import { Dialog, Box, TextField, Button, Typography } from '@mui/material';
import { ChecklistItemActions } from './ChecklistItem';
import type { ChecklistItemProps } from './ChecklistItem';
import DatePicker from 'core/DatePicker';
import AssigneePicker from './AssigneePicker';

interface ChecklistItemDialogProps {
  mode: 'add' | 'edit';
  open: boolean;
  onClose: () => void;
  item?: ChecklistItemProps;
  onDeleteClick?: () => void;
  onSave: (item: ChecklistItemProps) => void;
  onUpdate: (item: ChecklistItemProps) => void;
}

const demoAssignees: string[] = ['John Doe', 'Jane Doe', 'John Smith', 'Jane Smith'];

const ChecklistItemDialog: React.FC<ChecklistItemDialogProps> = (props) => {
  const { mode, open, onClose, item, onDeleteClick, onSave, onUpdate } = props;
  const { id, text, completed, dueDate, assignees } = item || {};

  const [tempText, setTempText] = React.useState<string>('');
  const [tempDueDate, setTempDueDate] = React.useState<Date | string | undefined>();
  const [tempAssignees, setTempAssignees] = React.useState<string[]>([]);

  const [datePickerOpen, setDatePickerOpen] = React.useState<boolean>(false);
  const [assigneePickerOpen, setAssigneePickerOpen] = React.useState<boolean>(false);

  React.useEffect(() => {
    if (mode === 'edit') {
      setTempText(text || '');
      setTempDueDate(dueDate);
      setTempAssignees(assignees || []);
    }
    if (mode === 'add') {
      setTempText('');
      setTempDueDate(undefined);
      setTempAssignees([]);
    }
  }, [text, dueDate, assignees, mode]);

  const handleSaveClick = () => {
    const newItem: ChecklistItemProps = {
      id: id || Math.random().toString(),
      text: tempText,
      completed: completed || false,
      dueDate: new Date(tempDueDate || '07/07/2023').toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' }),
      assignees: tempAssignees,
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
            assignees={tempAssignees}
            onDeleteClick={onDeleteClick}
            setDatePickerOpen={setDatePickerOpen}
            setAssigneePickerOpen={setAssigneePickerOpen}
          />
        </Box>
      </Box>
      <Dialog open={datePickerOpen} onClose={() => setDatePickerOpen(false)}>
        <DatePicker endDate={tempDueDate} setEndDate={setTempDueDate} onClose={() => setDatePickerOpen(false)} />
      </Dialog>
      <AssigneePicker possible={demoAssignees} chosen={tempAssignees} setChosen={setTempAssignees} open={assigneePickerOpen} onClose={() => setAssigneePickerOpen(false)} />
    </Dialog>
  );
}

export default ChecklistItemDialog;