import React from 'react';

import { List, ListItem, ListItemIcon, ListItemText, IconButton, Dialog, Button } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';

interface AssigneePickerProps {
  possible: string[];
  chosen: string[];
  setChosen: (value: React.SetStateAction<string[]>) => void;
  open: boolean;
  onClose: () => void;
}

const AssigneePicker: React.FC<AssigneePickerProps> = (props) => {
  const { possible, chosen, setChosen, open, onClose } = props;

  const handleAssigneesChange = (assignee: string, e: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    if (chosen.includes(assignee)) {
      setChosen(chosen.filter((a) => a !== assignee));
    }
    else {
      setChosen([...chosen, assignee]);
    }
    e.stopPropagation();
  };

  const handleClose = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    onClose();
    e.stopPropagation();
  }

  return (
    <Dialog open={open} onClose={onClose}>
      <List>
        {possible.map((assignee, index) => (
          <ListItem key={index}>
            <ListItemIcon>
              <IconButton onClick={(e) => handleAssigneesChange(assignee, e)}>
                {chosen.includes(assignee) ? <CheckBoxIcon color='primary' /> : <CheckBoxOutlineBlankIcon />}
              </IconButton>
            </ListItemIcon>
            <ListItemText>{assignee}</ListItemText>
          </ListItem>
        ))}
      </List>
      <Button
        variant='contained'
        color='primary'
        onClick={(e) => handleClose(e)}
      >
        Done
      </Button>
    </Dialog>
  );
}

export default AssigneePicker;