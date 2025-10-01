import React from 'react';
import { alpha, Chip, styled, Typography } from '@mui/material';
import { TaskApi } from '@dxs-ts/task-api';


interface IndicatorRoleProps {
  taskRoles: string;
  roles: TaskApi.Role[];
}

export const IndicatorRole: React.FC<IndicatorRoleProps> = ({ taskRoles, roles }) => {
  if (!Array.isArray(taskRoles)) {
    return (
      <IndicatorRoleRoot>
        <Typography>--</Typography>
      </IndicatorRoleRoot>)
  }
 
  return (
    <IndicatorRoleRoot>
      {taskRoles.map(taskRole => <Chip variant='outlined' size='small' label={roles.find(role=>role.id === taskRole)?.groupName ?? taskRole}/>)}
    </IndicatorRoleRoot>
  )
}




const MUI_NAME = 'IndicatorRoleRootClassName';
const IndicatorRoleRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    '.MuiAvatar-root': {
      height: '25px',
      width: '25px',
    }
  };
})