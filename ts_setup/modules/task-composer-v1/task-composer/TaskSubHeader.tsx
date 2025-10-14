import React from 'react';

import { Autocomplete, Box, Checkbox, Grid2, TextField } from "@mui/material";
import { CheckBox as CheckBoxIcon } from '@mui/icons-material';
import { CheckBoxOutlineBlank as CheckBoxOutlineBlankIcon } from '@mui/icons-material';

import { FormattedMessage, useIntl } from "react-intl";
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';

import { TaskFormDelegateProps } from "./TaskFormState";
import RadioGroupPopover from './RadioGroupPopover';

export const classes = {
  taskRoleList: {
    display: "flex",
    flexWrap: "wrap",
    paddingLeft: 1,
    paddingBottom: 1,
    gap: 1,
  },
  taskRoleFieldset: {
    border: `1px solid lightgrey`,
    borderRadius: 5,
    width: "100%",
    padding: 1
  },
  taskRoleLegend: {
    marginLeft: 8,
    paddingLeft: 24
  }
};

export type TaskRoleSelectProps = {
  assignedRoles: string[]
  groups: TaskApi.Role[]
  acceptNewRoles: (selectedRoles: TaskApi.Role[]) => void
}


const icon = <CheckBoxOutlineBlankIcon fontSize="small" />;
const checkedIcon = <CheckBoxIcon fontSize="small" />;

const TaskRoleSelect: React.FC<TaskRoleSelectProps> = ({ assignedRoles, groups, acceptNewRoles }) => {
  const { formatMessage } = useIntl();
  const [roles, setRoles] = React.useState(groups.filter(g => assignedRoles.includes(g.id)));

  const handleChange = (event: any, newValue: TaskApi.Role[]) => {
    setRoles(newValue);
    acceptNewRoles(newValue);
  };

  return (
    <Autocomplete multiple value={roles} options={groups}
      onChange={handleChange}
      disableCloseOnSelect
      getOptionLabel={(option) => option.groupName}
      renderOption={(props, option, { selected }) => (
        <li {...props} key={option.groupName}>
          <Checkbox
            icon={icon}
            checkedIcon={checkedIcon}
            checked={selected}
          />
          {option.groupName}
        </li>
      )}
      renderInput={(params) => (
        <TextField {...params} value={roles} label={formatMessage({ id: 'taskDialog.assignedTo' })}
          placeholder={formatMessage({ id: 'taskDialog.assignedTo' })}
          autoFocus={true}
          slotProps={{
            inputLabel: {
              shrink: true,
            },
          }}
        />
      )}
    />
  )
}


export interface TaskSubHeader {
  readOnly: boolean;
  form: TaskFormDelegateProps;
  slots: {
    statusExtra: React.ReactNode;
  }
}

export const TaskSubHeader: React.FC<TaskSubHeader> = (props) => {
  const { readOnly, form } = props;
  const { errors, currentState, setFieldValue } = form;
  const { assignedRoles } = currentState;
  const backend = useTaskBackend();
  const [userList, setUserList] = React.useState<TaskApi.User[]>([]);
  const groups = backend.roles;
  

  React.useEffect(() => {
    if (assignedRoles && assignedRoles.length > 0) {
      backend.persistence.findAllUsers(assignedRoles).then(setUserList);
    } else {
      setUserList([]);
    }
  }, [assignedRoles])

  return (<>

    <Box sx={{ p: 2, mb: 2, mt: 2 }}>
      <Grid2 container spacing={2}>
        {!!groups.length &&
          <Grid2 size={{ xs: 12, md: 6 }}>

            <TaskRoleSelect assignedRoles={assignedRoles ?? []} groups={groups}
              acceptNewRoles={(roles: TaskApi.Role[]) => {
                const groupList = roles.map(r => r.id);
                setFieldValue("assignedRoles", groupList);
              }}
            />
          </Grid2>
        }
        {<Grid2 size={{ xs: 12, md: !!groups.length ? 6 : 12 }}>
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
          {readOnly && (
            <TextField
              sx={{ marginTop: 1 }}
              name='assignedUser'
              value={currentState.assignedUser}
              fullWidth={true}
              inputProps={{
                readOnly: readOnly
              }}
              label={<FormattedMessage id='taskDialog.assignedUser' />}
              InputLabelProps={{ shrink: true }}
            >
            </TextField>
          )
          }
        </Grid2>
        }

        <Grid2 size={{ xs: 12, md: 6 }}>
          <Box display='flex' flexDirection='row' alignItems='flex-end' gap={2}>
            <RadioGroupPopover
              label={<FormattedMessage id='taskDialog.status' />}
              readonly={readOnly}
              messages={TaskApi.task_status_messages}
              colorMap={TaskApi.task_status_colors}
              invalidValues={[TaskApi.TaskStatus.TRANSFERRED]}
              handleCallback={newValue => setFieldValue('status', newValue as any)}
              value={currentState.status}
            />
            <Box>{props.slots.statusExtra}</Box>
          </Box>

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
    </Box>
  </>
  );
}