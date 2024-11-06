import React, { useState } from 'react';
import { Autocomplete, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, TextField, Container } from "@mui/material";
import CheckBoxIcon from '@mui/icons-material/CheckBox';

import { FormattedMessage, useIntl } from "react-intl";

import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';

import { UserGroup } from '../../types/UserGroup';
import * as Burger from '@/burger';

export type TaskRoleDialogProps = {
  assignedRoles: string[]
  groups: UserGroup[]
  closeDialog: () => void
  acceptDialog: (selectedRoles: UserGroup[]) => void
}

const icon = <CheckBoxOutlineBlankIcon fontSize="small" />;
const checkedIcon = <CheckBoxIcon fontSize="small" />;


export const TaskRoleDialog: React.FC<TaskRoleDialogProps> = ({ assignedRoles, groups, closeDialog, acceptDialog }) => {
  const { formatMessage } = useIntl();
  const [roles, setRoles] = useState(groups.filter(g => assignedRoles.includes(g.id)));

  const handleClose = (event: any, reason: string) => {
    if (reason && reason === "backdropClick") {
      return;
    }
    closeDialog();
  }
  return (
    <Dialog open={true} onClose={handleClose} >
      <DialogTitle fontWeight='bold' id="role-dialog-title"><FormattedMessage id={'task.editRoles'} /></DialogTitle>
      <DialogContent>
        <Container maxWidth='md'>
          <Autocomplete
            multiple
            value={roles}
            id="checkboxes-tags-demo"
            options={groups}
            onChange={(event, newValue) => {
              setRoles(newValue);
            }}
            disableCloseOnSelect
            getOptionLabel={(option) => option.groupName}
            renderOption={(props, option, { selected }) => (
              <li {...props}>
                <Checkbox
                  icon={icon}
                  checkedIcon={checkedIcon}
                  style={{ marginRight: 8 }}
                  checked={selected}
                />
                {option.groupName}
              </li>
            )}
            style={{ width: 500, height: '30vh', padding: 2 }}
            renderInput={(params) => (
              <TextField {...params} value={roles} label={formatMessage({ id: 'taskDialog.assignedTo' })}
                placeholder={formatMessage({ id: 'taskDialog.assignedTo' })}
                sx={{ marginTop: 2 }}
                autoFocus={true}
              />
            )}
          />
        </Container>
      </DialogContent>
      <DialogActions>
        <Burger.SecondaryButton onClick={closeDialog} label='button.cancel' />
        <Burger.PrimaryButton onClick={() => acceptDialog(roles)} label='button.accept' />
      </DialogActions>
    </Dialog>
  )
}