import React from 'react';

import { Typography, ListItem, ListItemButton, ListItemText, IconButton, Dialog } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import EventIcon from '@mui/icons-material/Event';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import DeleteIcon from '@mui/icons-material/Delete';

import DatePicker from '../DatePicker';
import AssigneePicker from './AssigneePicker';

interface ChecklistItem {
  id: string;
  text: string;
  completed: boolean;
  dueDate?: string;
  assignees?: string[];
}

interface ChecklistItemProps {
  item: ChecklistItem;
  onChecked: () => void;
  onDeleteClick: () => void;
  onClick: (item: ChecklistItem) => void;
}

interface ChecklistItemActionProps {
  mode: 'add' | 'edit';
  dueDate: Date | string | undefined;
  setDueDate: (value: React.SetStateAction<Date | string | undefined>) => void;
  assignees: string[] | [];
  setAssignees: (value: React.SetStateAction<string[]>) => void;
  onDeleteClick?: () => void;
}

const ChecklistRow: React.FC<ChecklistItemProps> = (props) => {
  const { item, onChecked, onDeleteClick, onClick } = props;
  const { text, completed, dueDate, assignees } = item;
  const [checked, setChecked] = React.useState<boolean>(completed);
  const checkedTextStyle = checked ? { textDecoration: 'line-through' } : {};
  const [hovering, setHovering] = React.useState<boolean>(false);
  const [dueDateTemp, setDueDateTemp] = React.useState<Date | string | undefined>(dueDate);
  const [assigneesTemp, setAssigneesTemp] = React.useState<string[]>(assignees || []);

  const handleClick = () => {
    setChecked(!checked);
    onChecked();
  };

  const handleMouseOver = () => {
    setHovering(true);
  };

  const handleMouseOut = () => {
    setHovering(false);
  };

  return (
    <ListItem sx={{ height: '50px' }}>
      <IconButton onClick={handleClick}>
        {checked ? <CheckBoxIcon color='primary' /> : <CheckBoxOutlineBlankIcon />}
      </IconButton>
      <ListItemButton onMouseOver={handleMouseOver} onMouseOut={handleMouseOut} onClick={() => onClick(item)}>
        <ListItemText>
          <Typography sx={checkedTextStyle}>{text}</Typography>
        </ListItemText>
        {hovering && <ChecklistItemActions mode='edit' dueDate={dueDateTemp} setDueDate={setDueDateTemp} assignees={assigneesTemp} setAssignees={setAssigneesTemp} onDeleteClick={onDeleteClick} />}
      </ListItemButton>
    </ListItem>
  );
}

const demoAssignees: string[] = ['John Doe', 'Jane Doe', 'John Smith', 'Jane Smith'];

const ChecklistItemActions: React.FC<ChecklistItemActionProps> = (props) => {
  const { mode, dueDate, setDueDate, assignees, setAssignees, onDeleteClick } = props;

  const [datePickerOpen, setDatePickerOpen] = React.useState<boolean>(false);
  const [assigneePickerOpen, setAssigneePickerOpen] = React.useState<boolean>(false);

  const dateButtonColor = dueDate ? 'primary' : 'inherit';
  const assigneesButtonColor = assignees.length ? 'primary' : 'inherit';

  const handleDateClick = (e: React.MouseEvent<HTMLElement>) => {
    setDatePickerOpen(true);
    e.stopPropagation();
  };

  const handleAssigneesClick = (e: React.MouseEvent<HTMLElement>) => {
    setAssigneePickerOpen(true);
    e.stopPropagation();
  };

  const handleDelete = (e: React.MouseEvent<HTMLElement>) => {
    onDeleteClick && onDeleteClick();
    e.stopPropagation();
  };

  return (
    <>
      <IconButton onClick={handleDateClick}>
        <EventIcon color={dateButtonColor} />
      </IconButton>
      {dueDate && <Typography>{new Date(dueDate).toLocaleDateString()}</Typography>}
      <IconButton onClick={handleAssigneesClick}>
        <AccountCircleIcon color={assigneesButtonColor} />
      </IconButton>
      {assignees && <Typography>{assignees.join(', ')}</Typography>}
      {mode === 'edit' && <IconButton onClick={handleDelete}>
        <DeleteIcon color='error' />
      </IconButton>}
      <Dialog open={datePickerOpen} onClose={() => setDatePickerOpen(false)}>
        <DatePicker endDate={dueDate} setEndDate={setDueDate} />
      </Dialog>
      <AssigneePicker possible={demoAssignees} chosen={assignees} setChosen={setAssignees} open={assigneePickerOpen} onClose={() => setAssigneePickerOpen(false)} />
    </>
  );
}

export type { ChecklistItem, ChecklistItemActionProps };
export { ChecklistItemActions }
export default ChecklistRow;