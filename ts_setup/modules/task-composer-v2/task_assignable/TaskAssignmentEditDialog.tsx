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
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { useQuery } from '@tanstack/react-query';


export interface TaskAssignmentEditDialogProps {
  open: boolean,
  taskId: string,
  onClose: () => void,
}


export const TaskAssignmentEditDialog: React.FC<TaskAssignmentEditDialogProps> = ({ open, onClose, taskId }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const [selectedForms, setSelectedForms] = React.useState<TaskApi.FormAssignment[]>([]);
  const { task } = useTaskDashboard();

  async function handleSave() {
  //await saveCustomerComment({ commentText: newMessage });
  //await backend.persistence.createManyTaskCustomerAssignments()
  }

  function handleCloseDialog() {
    onClose();
  }

  const backend = useTaskBackend();

  const { data: options, error, refetch, isPending } = useQuery({
    queryKey: ['dialob-dashboard'],
    queryFn: () => backend.persistence.findAllTaskFormAssignments(taskId),
    initialData: []
  });



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
        <Autocomplete
          multiple
          disableCloseOnSelect
          options={options}
          getOptionLabel={(option) => option.serviceName}
          onChange={(_event, newValue) => setSelectedForms(newValue)}
          renderOption={(props, option, { selected }) => (
            <li {...props} key={option.serviceName}>
              <Checkbox icon={<CheckBoxOutlineBlankIcon fontSize="small" />} checkedIcon={<CheckBoxIcon fontSize="small" />} checked={selected} />
              {option.serviceName}
            </li>
          )}
          renderInput={(params) => (<TextField {...params} placeholder="Select forms" autoFocus />)}
        />
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={handleCloseDialog}>{intl.formatMessage({ id: 'button.close' })}</Button>
        <Button onClick={handleSave} disabled={selectedForms.length === 0}>{intl.formatMessage({ id: 'button.accept' })}</Button>
      </DialogActions>
    </StyledDialog>
  )
}






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
