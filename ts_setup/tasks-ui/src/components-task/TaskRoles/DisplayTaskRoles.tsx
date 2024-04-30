import React from 'react';
import { AvatarGroup, Button, Stack } from '@mui/material';

import { RoleId } from 'descriptor-access-mgmt';
import { AvatarEmpty, AvatarUser } from 'components-generic';


export const DisplayTaskRoles: React.FC<{
  roles: RoleId[],
  onClick: (event: React.MouseEvent<HTMLElement>) => void;
  fullnames?: boolean,
  disabled?: boolean | undefined
}> = ({ roles, onClick, fullnames, disabled }) => {

  function handleOnClick(event: React.MouseEvent<HTMLElement>) {
    if (disabled === undefined || disabled === false) {
      onClick(event);
    }
  }

  if (roles.length === 0) {
    return (<Button disabled={disabled} variant='text' color='inherit' sx={{ "&.MuiButtonBase-root": { minWidth: "unset" } }} onClick={handleOnClick}>
      <AvatarEmpty />
    </Button>);
  }

  if (fullnames) {
    return (<Stack spacing={1} onClick={handleOnClick}>
      {roles.map(role => (<AvatarUser key={role} fullname>{role}</AvatarUser>))}
    </Stack>);
  }

  return (<Button disabled={disabled} variant='text' color='inherit' sx={{ "&.MuiButtonBase-root": { minWidth: "unset" } }} onClick={handleOnClick}>
    <AvatarGroup spacing='medium'>
      {roles.map(assignee => (<AvatarUser key={assignee} children={assignee} />))}
    </AvatarGroup>
  </Button>);
}
