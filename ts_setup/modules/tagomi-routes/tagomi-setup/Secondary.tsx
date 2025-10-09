import React from 'react';
import { MenuList, MenuItem, ListItemText } from '@mui/material';

import { useIntl } from 'react-intl';


import { useTagomiNav } from '../tagomi-nav';
import { ActivityProps, useActivities } from './Activities';

import { EveliPermissions, EveliShellExplorer, EveliShellCompose, _eveli_shell_useUtilityClasses as useUtilityClasses } from '@dxs-ts/eveli-primitives';
import { EveliTenantFeatureEnabled } from '@dxs-ts/eveli-api';



const ActivitiesViewItem: React.FC<{ data: ActivityProps, onClick: () => void }> = (props) => {
  const [open, setOpen] = React.useState<boolean>(false);
  const handleClose = () => {
    setOpen(false)
    //props.onClick();
  };
  const handleOpen = () => {
    setOpen(true);
  }
  const Composer: React.FC<{ onClose: () => void }> = open === false ? () => (<></>) : props.data.composer;
  return (
    <>
      <Composer onClose={handleClose} />
      <MenuItem onClick={handleOpen}>
        <ListItemText>{props.data.buttonCreate}</ListItemText>
      </MenuItem>
    </>
  )
}

const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { activeItem, onNav } = useTagomiNav();
  const activities = useActivities();
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);

  const [migrationsDialogOpen, setMigrationsDialogOpen] = React.useState(false)

  const handleComposeSelectClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  }
  const handleComposeSelectClose = () => {
    setAnchorEl(null);
  }

  return (
    <>
      <EveliShellCompose open={!!anchorEl} anchorEl={anchorEl} onClose={handleComposeSelectClose}>
        <MenuList>
          {activities.map((activity, index) => (<ActivitiesViewItem key={index} data={activity} onClick={handleComposeSelectClose} />))}
        </MenuList>
      </EveliShellCompose>


      <EveliShellExplorer>
        <>...</>
      </EveliShellExplorer>
    </>
  )
}
export { Secondary }


