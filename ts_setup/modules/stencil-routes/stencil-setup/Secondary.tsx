import React from 'react';
import { Button, MenuList, MenuItem, ListItemText } from '@mui/material';
import { CreateOutlined as CreateOutlinedIcon } from '@mui/icons-material';
import { MenuBookOutlined as MenuBookOutlinedIcon } from '@mui/icons-material';
import { AccountTreeOutlined as AccountTreeOutlinedIcon } from '@mui/icons-material';
import { InsertLinkOutlined as InsertLinkOutlinedIcon } from '@mui/icons-material';
import { TranslateOutlined as TranslateOutlinedIcon } from '@mui/icons-material';
import { FormatShapesOutlined as FormatShapesOutlinedIcon } from '@mui/icons-material';
import { NewReleasesOutlined as NewReleasesOutlinedIcon } from '@mui/icons-material';
import { HelpOutlineOutlined as HelpOutlineOutlinedIcon } from '@mui/icons-material';

import { useIntl } from 'react-intl';


import { MigrationComposer } from '../stencil-migration';
import { useStencilNav } from '../stencil-nav';
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
  const { activeItem, onNav } = useStencilNav();
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

      {migrationsDialogOpen && <MigrationComposer onClose={() => setMigrationsDialogOpen(false)} />}

      <EveliShellExplorer>
        <EveliPermissions id='CREATE_STENCIL_ASSET'>
          <Button startIcon={<CreateOutlinedIcon />}
            className={classes.composeButton}
            onClick={handleComposeSelectClick}>
            {intl.formatMessage({ id: 'menu.compose' })}
          </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_STENCIL_ARTICLES'>
          <Button variant={activeItem?.type === 'ARTICLES' ? 'explorerActive' : 'explorerInactive'}
            startIcon={<MenuBookOutlinedIcon />}
            onClick={() => onNav({ type: 'ARTICLES' })}>
            {intl.formatMessage({ id: 'menu.articles' })}
          </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_STENCIL_SERVICES'>
          <Button variant={activeItem?.type === 'SERVICES' ? 'explorerActive' : 'explorerInactive'}
            startIcon={<AccountTreeOutlinedIcon />}
            onClick={() => onNav({ type: 'SERVICES' })}>
            {intl.formatMessage({ id: 'menu.services' })}
          </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_STENCIL_LINKS'>
          <Button variant={activeItem?.type === 'LINKS' ? 'explorerActive' : 'explorerInactive'}
            startIcon={<InsertLinkOutlinedIcon />}
            onClick={() => onNav({ type: 'LINKS' })}>
            {intl.formatMessage({ id: 'menu.links' })}
          </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_STENCIL_LOCALES'>
          <Button variant={activeItem?.type === 'LOCALES' ? 'explorerActive' : 'explorerInactive'}
            startIcon={<TranslateOutlinedIcon />}
            onClick={() => onNav({ type: 'LOCALES' })}>
            {intl.formatMessage({ id: 'menu.locales' })}
          </Button>
        </EveliPermissions>

        <EveliPermissions id='NAV_TO_STENCIL_TEMPLATES'>
          <Button variant={activeItem?.type === 'TEMPLATES' ? 'explorerActive' : 'explorerInactive'}
            startIcon={<FormatShapesOutlinedIcon />}
            onClick={() => onNav({ type: 'TEMPLATES' })}>
            {intl.formatMessage({ id: 'menu.templates' })}
          </Button>
        </EveliPermissions>

        <Button variant='explorerInactive'
          startIcon={<HelpOutlineOutlinedIcon />}
          onClick={() => window.open("https://github.com/the-stencil-io/the-stencil-composer/wiki", "_blank")}>
          {intl.formatMessage({ id: 'menu.help' })}
        </Button>
      </EveliShellExplorer>
    </>
  )
}
export { Secondary }


