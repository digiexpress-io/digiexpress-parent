import React from 'react';
import {
  Autocomplete, Box, Button, Checkbox, Dialog, DialogActions,
  DialogContent, DialogContentText, DialogTitle, Divider, FormControl,
  generateUtilityClass, MenuItem, Select, Stack, styled, TextField, Typography, Zoom
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
  const [selectedLocale, setSelectedLocale] = React.useState<string>("");

  const { task } = useTaskDashboard();

  async function handleSave() {
    //await saveCustomerComment({ commentText: newMessage });
    await backend.persistence.createManyTaskCustomerAssignments([

    ])
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

  const formLocales = options.flatMap(o => o.locales);
  const availableLocales: string[] = formLocales.reduce<string[]>((acc, locale) => {
    if (!acc.includes(locale)) {
      acc.push(locale)
    }
    return acc;
  }, []);

  React.useEffect(() => {
    if (availableLocales.length === 1) {
      setSelectedLocale(availableLocales[0])
    }
  }, [availableLocales])

  const filteredFormOptions = React.useMemo(() => {
    if (!selectedLocale) {
      return [];
    }
    return options.filter(o => o.locales.includes(selectedLocale))
  }, [options, selectedLocale])


  return (
    <StyledDialog fullWidth maxWidth='md' className={classes.taskAssignmentEdit} open={open} onClose={onClose} slots={{ transition: Zoom }}>

      <DialogTitle>
        {intl.formatMessage({ id: 'task.assignable', defaultMessage: 'Assign forms to customer' })}
      </DialogTitle>

      <DialogContent>
        <Stack direction='column' width='100%'>
          <Box className={classes.contentText}>
            <InfoOutlinedIcon fontSize='small' color='info' />
            <Typography>
              {task.clientIdentificator}{intl.formatMessage({ id: 'task.assignable.desc', defaultMessage: ' will be assigned the selected forms in the chosen language.' })}
            </Typography>
          </Box>
          <Divider sx={{ my: 2 }} />

          <Box className={classes.contentBody}>
            <div>
              <Typography variant='subtitle2'>{intl.formatMessage({ id: 'task.assignable.locale', defaultMessage: 'Language' })}</Typography>
              <FormControl size="medium" sx={{ minWidth: 100 }}>
                <Select value={selectedLocale}
                  onChange={(e) => setSelectedLocale(e.target.value)}
                  disabled={availableLocales.length === 1}
                  displayEmpty
                >
                  {availableLocales.map(locale => (
                    <MenuItem key={locale} value={locale}>
                      {locale}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </div>
            <Box flex={1}>
              <Typography variant='subtitle2'>{intl.formatMessage({ id: 'task.assignable.forms', defaultMessage: 'Forms' })}</Typography>
              <Autocomplete fullWidth
                multiple
                disableCloseOnSelect
                options={filteredFormOptions}
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
            </Box>
          </Box>
        </Stack>
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
      overflow: 'hidden'
    },
    '& .TaskAssignmentEdit-contentText': {
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1)
    },
    '& .TaskAssignmentEdit-contentBody': {
      display: 'flex',
      flexDirection: 'row',
      gap: theme.spacing(1)
    }

  };
})


const useUtilityClasses = () => {
  const slots = {
    taskAssignmentEdit: ['taskAssignmentEdit'],
    contentText: ['contentText'],
    contentBody: ['contentBody'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
