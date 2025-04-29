import React from 'react';

import { Autocomplete, Checkbox, ClickAwayListener, Grid2, Paper, TextField } from "@mui/material";
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';

import { FormattedMessage, useIntl } from "react-intl";

import { useFetch } from "@dxs-ts/eveli-fetch";
import { IamApi } from "@/api-iam";
import { TaskApi } from '@/api-task';

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
  groups: IamApi.UserGroup[]
  acceptNewRoles: (selectedRoles: IamApi.UserGroup[]) => void
}


const icon = <CheckBoxOutlineBlankIcon fontSize="small" />;
const checkedIcon = <CheckBoxIcon fontSize="small" />;

const TaskRoleSelect: React.FC<TaskRoleSelectProps> = ({ assignedRoles, groups, acceptNewRoles}) => {
  const { formatMessage } = useIntl();
  const [roles, setRoles] = React.useState(groups.filter(g => assignedRoles.includes(g.id)));

  const handleClose = () => {
    acceptNewRoles(roles);
  };

  const handleChange = (event: any, newValue: IamApi.UserGroup[]) => {
    setRoles(newValue);
    acceptNewRoles(newValue); 
  };

  return (
    <ClickAwayListener onClickAway={handleClose}>
      <Autocomplete multiple value={roles} options={groups} 
        onChange={handleChange}
        disableCloseOnSelect
        getOptionLabel={(option) => option.groupName}
        renderOption={(props, option, { selected }) => (
          <li {...props}>
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
      </ClickAwayListener>
    )
}


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

  React.useEffect(() => {
    if (assignedRoles && assignedRoles.length > 0) {
      getUsers(assignedRoles).then(setUserList);
    } else {
      setUserList([]);
    }
  }, [assignedRoles])




  function findRoleDescription(role: string) {
    return groups.find((group: any) => group.id === role)?.groupName || role;
  }

  return (
    <Paper elevation={2} sx={{ p: 2, mb: 2, mt: 2 }}>
      <Grid2 container spacing={2}>
        {!!groups.length &&
          <Grid2 size={{ xs: 12, md: 6 }}>
            
          <TaskRoleSelect assignedRoles={assignedRoles ?? []}  groups={groups}
            acceptNewRoles={(roles: IamApi.UserGroup[]) => { 
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
          {readOnly &&(
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
  );
}