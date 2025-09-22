import React from 'react';
import {
  Autocomplete, Box, Button, Checkbox, Dialog, DialogActions,
  DialogContent, DialogTitle, generateUtilityClass, styled, TextField, Typography, Zoom
} from '@mui/material';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { useTaskDashboard } from '../task-dashboard';


export interface TaskAssignmentEditDialogProps {
  open: boolean,
  onClose: () => void
}


export const TaskAssignmentEditDialog: React.FC<TaskAssignmentEditDialogProps> = ({ open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const [newMessage, setNewMessage] = React.useState<string>('');
  const { task, saveCustomerComment } = useTaskDashboard();

  async function handleSendMessage() {
    await saveCustomerComment({ commentText: newMessage });
    setNewMessage('')
  }

  function handleCloseDialog() {
    onClose();
  }



  return (
    <StyledDialog fullWidth maxWidth='md' className={classes.taskAssignmentEdit} open={open} onClose={onClose} slots={{ transition: Zoom }}>

      <DialogTitle>
        {intl.formatMessage({ id: 'task.assignable', defaultMessage: 'Assign forms to customer' })}
      </DialogTitle>

      <DialogContent>
        <Box display='flex' alignItems='center' gap={1}>
          <InfoOutlinedIcon fontSize='small' color='info' />
          <Typography>
            {task.clientIdentificator}{intl.formatMessage({ id: 'task.assignable.desc', defaultMessage: ' will be assigned the selected forms.' })}
          </Typography>
        </Box>
        <SelectFormsToAssign />
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={handleCloseDialog}>{intl.formatMessage({ id: 'button.close' })}</Button>
        <Button onClick={handleSendMessage} disabled={!newMessage.trim()}>{intl.formatMessage({ id: 'button.accept' })}</Button>
      </DialogActions>
    </StyledDialog>
  )
}




const options = ['Application for inspection', 'Building permit', 'Release and user agreement'];

export const SelectFormsToAssign: React.FC = () => {

  return (
    <Autocomplete multiple disableCloseOnSelect options={options} getOptionLabel={(option) => option}
      renderOption={(props, option, { selected }) => (
        <li {...props} key={option}>
          <Checkbox icon={<CheckBoxOutlineBlankIcon fontSize="small" />} checkedIcon={<CheckBoxIcon fontSize="small" />} checked={selected} />
          {option}
        </li>
      )}
      renderInput={(params) => (<TextField {...params} placeholder="Select forms" autoFocus />)}
    />
  );
};





const MUI_NAME = 'TaskAssignmentEdit';
const StyledDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'Messages',
  overridesResolver: (_props, styles) => {
    return [
      styles.taskAssignmentEdit
    ];
  },

})(({ theme }) => {

  return {
    height: '100vh',

    '.MuiDialogContent-root': {
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden'
    },


  };
})


const useUtilityClasses = () => {
  const slots = {
    taskAssignmentEdit: ['taskAssignmentEdit'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
