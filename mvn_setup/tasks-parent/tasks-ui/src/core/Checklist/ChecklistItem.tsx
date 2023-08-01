import React from 'react';

import { Typography, ListItem, ListItemButton, ListItemText, IconButton, Dialog, Box } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import EventIcon from '@mui/icons-material/Event';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import DeleteIcon from '@mui/icons-material/Delete';

import DatePicker from '../DatePicker';
import AssigneePicker from './AssigneePicker';

interface ChecklistItemProps {
  id: string;
  text: string;
  completed: boolean;
  dueDate?: string;
  assignees?: string[];
}

interface ChecklistItemComponentProps {
  item: ChecklistItemProps;
  onChecked: () => void;
  onDeleteClick: () => void;
  onClick: (item: ChecklistItemProps, e: React.MouseEvent<HTMLDivElement, MouseEvent>) => void;
}

interface ChecklistItemActionProps {
  mode: 'add' | 'edit';
  dueDate: Date | string | undefined;
  assignees: string[] | [];
  variant?: 'normal' | 'hover';
  onDeleteClick?: () => void;
  setDatePickerOpen: (value: React.SetStateAction<boolean>) => void;
  setAssigneePickerOpen: (value: React.SetStateAction<boolean>) => void;
}

const demoAssignees: string[] = ['John Doe', 'Jane Doe', 'John Smith', 'Jane Smith'];

const ChecklistItemActions: React.FC<ChecklistItemActionProps> = (props) => {
  const { mode, dueDate, assignees, variant, onDeleteClick, setAssigneePickerOpen, setDatePickerOpen } = props;

  const hoverSx = variant === 'hover' ? { position: 'absolute', zIndex: 1, top: '10%', right: 0, backgroundColor: '#F5F5F5' } : {};
  const dateButtonColor = dueDate ? 'primary' : 'inherit';
  const assigneesButtonColor = assignees.length ? 'primary' : 'inherit';

  const handleDateClick = (e: React.MouseEvent<HTMLElement>) => {
    e.stopPropagation();
    setDatePickerOpen(true);
  };

  const handleAssigneesClick = (e: React.MouseEvent<HTMLElement>) => {
    e.stopPropagation();
    setAssigneePickerOpen(true);
  };

  const handleDelete = (e: React.MouseEvent<HTMLElement>) => {
    e.stopPropagation();
    onDeleteClick && onDeleteClick();
  };

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', ...hoverSx }}>
      <IconButton onClick={(e) => handleDateClick(e)}>
        <EventIcon color={dateButtonColor} />
      </IconButton>
      {dueDate && <Typography>{new Date(dueDate).toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' })}</Typography>}
      <IconButton onClick={(e) => handleAssigneesClick(e)}>
        <AccountCircleIcon color={assigneesButtonColor} />
      </IconButton>
      {assignees && <Typography>{assignees.join(', ')}</Typography>}
      {mode === 'edit' && <IconButton onClick={(e) => handleDelete(e)}>
        <DeleteIcon color='error' />
      </IconButton>}
    </Box>
  );
}

const ChecklistItem: React.FC<ChecklistItemComponentProps> = (props) => {
  const { item, onChecked, onDeleteClick, onClick } = props;
  const { text, completed, dueDate, assignees } = item;
  const [checked, setChecked] = React.useState<boolean>(completed);
  const checkedTextStyle = checked ? { textDecoration: 'line-through' } : {};
  const [hovering, setHovering] = React.useState<boolean>(false);
  const [dueDateTemp, setDueDateTemp] = React.useState<Date | string | undefined>(dueDate);
  const [assigneesTemp, setAssigneesTemp] = React.useState<string[]>(assignees || []);

  const [datePickerOpen, setDatePickerOpen] = React.useState<boolean>(false);
  const [assigneePickerOpen, setAssigneePickerOpen] = React.useState<boolean>(false);

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

  const handleClose = () => {
    setDatePickerOpen(false);
    setAssigneePickerOpen(false);
  };

  return (
    <ListItem sx={{ position: 'relative' }}>
      <IconButton onClick={handleClick}>
        {checked ? <CheckBoxIcon color='primary' /> : <CheckBoxOutlineBlankIcon />}
      </IconButton>
      <ListItemButton onMouseOver={handleMouseOver} onMouseOut={handleMouseOut} onClick={(e) => onClick(item, e)}>
        <ListItemText>
          <Typography sx={checkedTextStyle}>{text}</Typography>
        </ListItemText>
        {hovering &&
          <ChecklistItemActions
            mode='edit'
            dueDate={dueDateTemp}
            assignees={assigneesTemp}
            variant='hover'
            onDeleteClick={onDeleteClick}
            setDatePickerOpen={setDatePickerOpen}
            setAssigneePickerOpen={setAssigneePickerOpen}
          />}
      </ListItemButton>
      <Dialog open={datePickerOpen} onClose={handleClose}>
        <DatePicker endDate={dueDateTemp} setEndDate={setDueDateTemp} onClose={handleClose} />
      </Dialog>
      <AssigneePicker possible={demoAssignees} chosen={assigneesTemp} setChosen={setAssigneesTemp} open={assigneePickerOpen} onClose={handleClose} />
    </ListItem>

  );
}

export type { ChecklistItemProps, ChecklistItemActionProps };
export { ChecklistItemActions }
export default ChecklistItem;