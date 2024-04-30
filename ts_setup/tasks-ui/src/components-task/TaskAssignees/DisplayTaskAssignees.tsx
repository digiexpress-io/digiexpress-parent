import React from 'react';
import { AvatarGroup, Button, Stack } from '@mui/material';

import { AvatarEmpty, AvatarUser } from 'components-generic';
import { PrincipalId } from 'descriptor-access-mgmt';



export const DisplayTaskAssignees: React.FC<{
  onClick: (event: React.MouseEvent<HTMLElement>) => void;
  assigneeIds: PrincipalId[];
  fullnames?: boolean;
  disabled?: boolean;
}> = ({ assigneeIds, disabled, onClick, fullnames }) => {


  function handleOnClick(event: React.MouseEvent<HTMLElement>) {
    if (disabled === undefined || disabled === false) {
      onClick(event);
    }
  }


  if (assigneeIds.length === 0) {
    return (<Button disabled={disabled} variant='text' color='inherit' sx={{ "&.MuiButtonBase-root": { minWidth: "unset" } }} onClick={handleOnClick}>
      <AvatarEmpty />
    </Button>);
  }

  if (fullnames) {
    return (<Stack spacing={1} onClick={handleOnClick}>
      {assigneeIds.map(assignee => (<AvatarUser key={assignee} fullname>{assignee}</AvatarUser>))}
    </Stack>);
  }

  return (<Button disabled={disabled} variant='text' color='inherit' sx={{ "&.MuiButtonBase-root": { minWidth: "unset" } }} onClick={handleOnClick}>
    <AvatarGroup spacing='medium'>
      {assigneeIds.map(assignee => (<AvatarUser key={assignee} children={assignee} />))}
    </AvatarGroup>
  </Button>);

}