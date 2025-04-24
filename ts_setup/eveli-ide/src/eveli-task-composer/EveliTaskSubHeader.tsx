import React from 'react';

import { Autocomplete, Box, Button, Chip, Grid2, InputLabel, Paper, TextField } from "@mui/material";
import { FormattedMessage } from "react-intl";

import { useFetch } from "@dxs-ts/eveli-fetch";
import { IamApi } from "@/api-iam";
import { TaskApi } from '@/api-task';

import { TaskFormDelegateProps } from "./TaskFormState";
import { TaskRoleDialog } from './TaskRoleDialog';
import RadioGroupPopover from './RadioGroupPopover';

export const classes = {
  taskRoleList: {
    display: "flex",
    flexWrap: "wrap",
    gap: 1,
    paddingTop: 0,
    paddingBottom: 1,
    paddingX: 1
  },
  taskRoleFieldset: {
    borderWidth: 1,
    borderStyle: 'solid',
    borderRadius: 10,
    width: "90%",
    marginBottom: 8,
    minHeight: 64
  },
  taskRoleLegend: {
    marginLeft: 8,
    paddingLeft: 24
  },

};


export interface EveliTaskSubHeader {
  readOnly: boolean;
  form: TaskFormDelegateProps;
}

export const EveliTaskSubHeader: React.FC<EveliTaskSubHeader> = (props) => {
  const { readOnly, form } = props;
  const { errors, currentState, setFieldValue } = form;
  const { assignedRoles } = currentState;
  
  const { groups } = useFetch('$org/groupsList.GET', {});
  const { getUsers } = useFetch('$org/groupMembership.GET', {});
  const [userList, setUserList] = React.useState<IamApi.GroupMember[]>([]);
  const [dialogOpen, setDialogOpen] = React.useState(false)

  React.useEffect(() => {
    if (assignedRoles && assignedRoles.length > 0) {
      getUsers(assignedRoles).then(setUserList);
    } else {
      setUserList([]);
    }
  }, [assignedRoles])

  function handleDialogClose() {
    setDialogOpen(false);
  }
  function handleDialogOpen() {
    setDialogOpen(true);
  }

  function findRoleDescription(role: string) {
    return groups.find((group: any) => group.id === role)?.groupName || role;
  }

  return (
  <>
    {dialogOpen && <TaskRoleDialog
      assignedRoles={assignedRoles ?? []} 
      groups={groups}
      closeDialog={handleDialogClose} 
      acceptDialog={(roles: IamApi.UserGroup[]) => { 
        const groupList = roles.map(r => r.id);
        setFieldValue("assignedRoles", groupList);
        handleDialogClose(); 
      }}
    /> }
    <Paper elevation={2} sx={{ p: 2, mb: 2, mt: 2 }}>
      <Grid2 container spacing={2} alignItems="top">
        {!!groups.length &&
          <Grid2 size={{ xs: 12, md: 6 }}>
            <Box display="flex" alignItems="center">
              <fieldset style={classes.taskRoleFieldset}>
                <legend style={classes.taskRoleLegend}>
                  <InputLabel size='small' shrink={true}><FormattedMessage id='taskDialog.assignedTo' /></InputLabel>
                </legend>

                <Box id='task-role-list' sx={classes.taskRoleList}>
                  {currentState.assignedRoles?.map((value: any) => (
                    <Chip key={value} label={findRoleDescription(value)} />
                  ))}
                </Box>
              </fieldset>

            </Box>
            <Button disabled={readOnly} variant='contained' onClick={handleDialogOpen}  ><FormattedMessage id='button.editRoles' /></Button>
          </Grid2>
        }
        {<Grid2 size={{ xs: 12, md: !!groups.length ? 6 : 12 }} sx={{ mt: 1 }}>
          {!readOnly &&
            <Autocomplete
              id="assignedUser"
              freeSolo
              options={userList}
              getOptionLabel={option => (typeof option === "string") ? option : option.userName ?? ''}
              value={{ userName: currentState.assignedUser, userEmail: currentState.assignedUserEmail }}
              onInputChange={(event, newInputValue) => {

                if (newInputValue === currentState.assignedUser) {
                  return;
                }
                setFieldValue("assignedUserEmail", userList.find(el => el.userName === newInputValue)?.userEmail || '');
                setFieldValue("assignedUser", newInputValue);
              }}
              renderInput={(params) => (
                <TextField {...params}
                  name='assignedUser'
                  fullWidth={true}
                  label={<FormattedMessage id='taskDialog.assignedUser' />}
                  InputLabelProps={{
                    shrink: true,
                  }}
                  error={!!errors.assignedUser}
                  helperText={errors.assignedUser}
                />
              )}
            />
          }
          {readOnly &&
            <TextField
              name='assignedUser'
              value={currentState.assignedUser}
              fullWidth={true}
              inputProps={{
                readOnly: readOnly
              }}
              label={<FormattedMessage id='taskDialog.assignedUser' />}
              InputLabelProps={{
                shrink: true,
              }}
            >
            </TextField>
          }
        </Grid2>
        }

        <Grid2 size={{ xs: 12, md: 6 }}>
          <RadioGroupPopover 
            label={<FormattedMessage id='taskDialog.status' />}
            readonly={readOnly}
            messages={TaskApi.task_status_messages}
            colorMap={TaskApi.task_status_colors}
            handleCallback={newValue => setFieldValue('status', newValue as any)}
            value={currentState.status}
          />

        </Grid2>
        <Grid2 size={{ xs: 12, md: 6 }}>
          <RadioGroupPopover 
            label={<FormattedMessage id='taskDialog.priority' />}
            readonly={readOnly}
            messages={TaskApi.task_priority_messages}
            colorMap={TaskApi.task_priority_colors}
            value={currentState.priority}
            handleCallback={newValue => setFieldValue('priority', newValue as any)}
          />
        </Grid2>
      </Grid2>
    </Paper>
  </>
  );
}