import React from 'react';
import {
  Autocomplete, Box, Button, Checkbox, Dialog, DialogActions,
  DialogContent, DialogTitle, Divider, FormControl,
  generateUtilityClass, MenuItem, Select, Stack, styled, TextField, Typography, Zoom
} from '@mui/material';
import { CheckBox as CheckBoxIcon } from '@mui/icons-material';
import { InfoOutlined as InfoOutlinedIcon } from '@mui/icons-material';
import { CheckBoxOutlineBlank as CheckBoxOutlineBlankIcon } from '@mui/icons-material';
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
  const [assignedForms, setAssignedForms] = React.useState<TaskApi.FormAssignment[]>([]);
  const [cancelledForms, setCancelledForms] = React.useState<TaskApi.TaskCustomerAssignment[]>([]);
  const [selectedLocale, setSelectedLocale] = React.useState<string>("");

  const { task } = useTaskDashboard();
  const backend = useTaskBackend();


  const { data, isPending } = useQuery({
    queryKey: ['dialob-dashboard'],
    queryFn: () => backend.persistence.findAllTaskFormAssignments(taskId)
      .then(forms => ({
        forms,
        locales: Array.from(new Set(forms.map(o => o.locale)))
      })
      ),
    initialData: { forms: [], locales: [] }
  });

  React.useEffect(() => {
    if (!isPending && !selectedLocale) {
      setSelectedLocale(data.locales.sort()[0] ?? '');
    }
  }, [data, isPending, selectedLocale]);


  async function handleSave() {
    await backend.persistence.createManyTaskCustomerAssignments(taskId, assignedForms.map((service) => ({ taskId, serviceId: service.id })));
    onClose();
  }

  function handleCloseDialog() {
    onClose();
  }


  return (
    <StyledDialog fullWidth maxWidth='md' className={classes.taskAssignmentEdit} open={open} onClose={onClose} slots={{ transition: Zoom }}>

      <DialogTitle>
        {intl.formatMessage({ id: 'task.assignable.dialog.title' })}
      </DialogTitle>

      <DialogContent>
        <Stack direction='column' width='100%'>
          <Typography variant='h3' color='primary.main'>{intl.formatMessage({ id: 'task.assignable.new' })}</Typography>

          <Box className={classes.contentText}>
            <InfoOutlinedIcon fontSize='small' color='info' />
            <Typography>
              {task.clientIdentificator}{" "}{intl.formatMessage({ id: 'task.assignable.desc' })}
            </Typography>
          </Box>
          <Divider sx={{ my: 2 }} />

          <Box className={classes.contentBody}>
            <div>
              <Typography variant='subtitle2'>{intl.formatMessage({ id: 'task.assignable.locale' })}</Typography>
              <FormControl size="medium" sx={{ minWidth: 100 }}>
                <Select value={selectedLocale}
                  onChange={(e) => setSelectedLocale(e.target.value)}
                  disabled={data.locales.length === 1}
                  displayEmpty
                >
                  {data.locales.map(locale => (
                    <MenuItem key={locale} value={locale}>
                      {locale}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </div>
            <Box flex={1}>
              <Typography variant='subtitle2'>{intl.formatMessage({ id: 'task.assignable.forms' })}</Typography>
              <Autocomplete fullWidth
                multiple
                disableCloseOnSelect
                options={data.forms.filter(f => f.locale === selectedLocale)}
                getOptionLabel={(option) => option.serviceName}
                onChange={(_event, newValue) => setAssignedForms(newValue)}
                renderOption={(props, option, { selected }) => (
                  <li {...props} key={option.serviceName}>
                    <Checkbox icon={<CheckBoxOutlineBlankIcon fontSize="small" />} checkedIcon={<CheckBoxIcon fontSize="small" />} checked={selected} />
                    {option.serviceName}
                  </li>
                )}
                renderInput={(params) => (<TextField {...params} placeholder={intl.formatMessage({ id: 'task.assignable.selectForms' })} autoFocus />)}
              />
            </Box>
          </Box>

          <Box className={classes.cancelledFormsContainer}>
            <Typography variant="h3" color='error.main'>
              {intl.formatMessage({ id: 'task.assignable.cancel' })}
            </Typography>
            <Typography>
              {intl.formatMessage({ id: 'task.assignable.cancel.desc' })}
            </Typography>

            <Autocomplete
              fullWidth
              multiple
              disableCloseOnSelect
              options={task.customerAssignments.filter(c => c.status !== 'CANCELLED' && c.status !== 'COMPLETED')}
              value={cancelledForms}
              getOptionLabel={option => `${option.description} - ${option.locale}`}
              onChange={(_event, newValue) => setCancelledForms(newValue)}
              renderOption={(props, option, { selected }) => (
                <li {...props} key={option.id}>
                  <Checkbox
                    icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
                    checkedIcon={<CheckBoxIcon fontSize="small" />}
                    checked={selected}
                  />
                  {option.locale} – {option.description}
                </li>
              )}
              renderInput={params => <TextField {...params} placeholder={intl.formatMessage({ id: 'task.assignable.selectForms' })} />}
            />

            <Box sx={{ mt: 2 }}>
              {cancelledForms.length > 0 && <Typography color='error.main' fontWeight={500}>{intl.formatMessage({ id: 'task.assignable.cancel.formsList.desc' })}</Typography>}
              {cancelledForms.map((form, index) => (
                <Box key={form.id} sx={{ backgroundColor: index % 2 === 0 ? 'action.hover' : 'background.default', p: 0.5, display: 'flex' }}>
                  <Typography>{form.description}{" - "}{form.locale}{" - "}{form.status}</Typography>
                </Box>
              ))}
            </Box>
          </Box>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={handleCloseDialog}>{intl.formatMessage({ id: 'button.close' })}</Button>
        <CancelButton onClick={() => { }} disabled={cancelledForms.length === 0}>
          {intl.formatMessage({ id: 'button.cancelSelected' }, { count: cancelledForms.length })}
        </CancelButton>
        <Button onClick={handleSave} disabled={assignedForms.length === 0}>
          {intl.formatMessage({ id: 'button.acceptSelected' }, { count: assignedForms.length })}
        </Button>
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
    '.MuiDialogContent-root': {
      display: 'flex',
      overflow: 'scroll'
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
    },
    '& .TaskAssignmentEdit-cancelledFormsContainer': {
      marginTop: theme.spacing(3),
      border: `1px solid ${theme.palette.divider}`,
      borderRadius: theme.spacing(1),
      padding: theme.spacing(3),
      boxShadow: '-4px 4px 10px rgba(0, 0, 0, 0.06)',
    }
  };
})


const CancelButton = styled(Button)(({ theme }) => ({
  color: theme.palette.error.contrastText,
  backgroundColor: theme.palette.error.main,
  '&:hover': {
    backgroundColor: theme.palette.error.dark,
  },
  fontWeight: 600,
}));

const useUtilityClasses = () => {
  const slots = {
    taskAssignmentEdit: ['taskAssignmentEdit'],
    contentText: ['contentText'],
    contentBody: ['contentBody'],
    cancelledFormsContainer: ['cancelledFormsContainer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
