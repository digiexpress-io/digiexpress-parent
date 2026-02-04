import React from 'react';
import { MenuList, MenuItem, ListItemText, Button } from '@mui/material';
import { CreateOutlined as CreateOutlinedIcon } from '@mui/icons-material';
import { CodeOutlined as CodeOutlinedIcon } from '@mui/icons-material';
import { ImageOutlined as ImageOutlinedIcon } from '@mui/icons-material';
import { ArticleOutlined as ArticleOutlinedIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import { ActivityProps, useActivities } from './Activities';
import { useTagomiNav } from '../tagomi-nav';

import { EveliShellExplorer, EveliShellCompose, _eveli_shell_useUtilityClasses as useUtilityClasses } from '@dxs-ts/eveli-primitives';



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

export const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const activities = useActivities();
  const { onNav, activeItem } = useTagomiNav();
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);

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
        <Button startIcon={<CreateOutlinedIcon />}
          className={classes.composeButton}
          onClick={handleComposeSelectClick}>
          {intl.formatMessage({ id: 'menu.compose' })}
        </Button>

        <Button variant={activeItem?.type === 'SERVICES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<CodeOutlinedIcon />}
          onClick={() => onNav({ type: 'SERVICES' })}>
          {intl.formatMessage({ id: 'menu.services.tagomi' })}
        </Button>

        <Button variant={activeItem?.type === 'IMAGES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<ImageOutlinedIcon />}
          onClick={() => onNav({ type: 'IMAGES' })}>
          {intl.formatMessage({ id: 'menu.images.tagomi' })}
        </Button>

        <Button variant={activeItem?.type === 'SCRIPTS' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<ArticleOutlinedIcon />}
          onClick={() => onNav({ type: 'SCRIPTS' })}>
          {intl.formatMessage({ id: 'menu.scripts.tagomi' })}
        </Button>
      </EveliShellExplorer>
    </>
  )
}


