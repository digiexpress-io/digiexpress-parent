import React from 'react';

import { Box, Divider, Button, Typography, List, ListItem, ListItemButton, ListItemIcon, ListItemText, IconButton, SxProps } from '@mui/material';
import ChecklistIcon from '@mui/icons-material/Checklist';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import EventIcon from '@mui/icons-material/Event';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import DeleteIcon from '@mui/icons-material/Delete';

interface ChecklistItemProps {
  text: string;
  completed: boolean;
  dueDate?: string;
  assignees?: string[];
}

const ChecklistHeader: React.FC = () => {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', ml: 3 }}>
      <ChecklistIcon color='primary' sx={{ mr: 3 }} />
      <Typography variant="h4" fontWeight='h1.fontWeight'>Checklist</Typography>
    </Box>
  );
}

const ChecklistItem: React.FC<ChecklistItemProps> = (props) => {
  const { text, completed, dueDate, assignees } = props;
  const [checked, setChecked] = React.useState<boolean>(completed);
  const checkedTextStyle = checked ? { textDecoration: 'line-through' } : {};

  return (
    <ListItem>
      <IconButton onClick={() => setChecked(!checked)}>
        {checked ? <CheckBoxIcon color='primary' /> : <CheckBoxOutlineBlankIcon />}
      </IconButton>
      <ListItemButton>
        <ListItemText>
          <Typography sx={checkedTextStyle}>{text}</Typography>
        </ListItemText>
        <ChecklistItemActions />
      </ListItemButton>
    </ListItem>
  );
}

const ChecklistItemActions: React.FC<{ sx?: SxProps }> = ({ sx }) => {
  return (
    <Box sx={sx}>
      <IconButton>
        <EventIcon />
      </IconButton>
      <IconButton>
        <AccountCircleIcon />
      </IconButton>
      <IconButton>
        <DeleteIcon />
      </IconButton>
    </Box>
  );
}


const demoChecklistItems: ChecklistItemProps[] = [
  {
    text: 'Item 1',
    completed: true,
    dueDate: '2021-10-10',
    assignees: ['John Doe', 'Jane Doe'],
  },
  {
    text: 'Item 2',
    completed: false,
    dueDate: '2021-10-10',
    assignees: ['John Doe', 'Jane Doe'],
  },
  {
    text: 'Item 3',
    completed: false,
  },
];

const Checklist: React.FC = () => {
  return (
    <Box>
      <ChecklistHeader />
      <Divider sx={{ my: 1 }} />
      {demoChecklistItems.map((item, index) => <ChecklistItem key={index} {...item} />)}
    </Box>
  );
}

export { Checklist };
