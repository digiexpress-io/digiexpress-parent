import React from 'react';
import { Button, MenuList, MenuItem, ListItemText } from '@mui/material';
import CreateOutlinedIcon from '@mui/icons-material/CreateOutlined';
import MenuBookOutlinedIcon from '@mui/icons-material/MenuBookOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import InsertLinkOutlinedIcon from '@mui/icons-material/InsertLinkOutlined';
import TranslateOutlinedIcon from '@mui/icons-material/TranslateOutlined';
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined';
import FormatShapesOutlinedIcon from '@mui/icons-material/FormatShapesOutlined';
import NewReleasesOutlinedIcon from '@mui/icons-material/NewReleasesOutlined';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import { useIntl } from 'react-intl';

import { useUtilityClasses } from '../eveli-shell/useUtilityClasses';

import { MigrationComposer } from '../stencil-migration';
import { useStencilNav } from '../stencil-nav';
import { ActivityProps, useActivities } from './Activities';
import { EveliShellCompose } from '@/eveli-shell-compose';
import { EveliShellExplorer } from '@/eveli-shell-explorer';
import { EveliLogo } from '@/eveli-logo';




const ActivitiesViewItem: React.FC<{ data: ActivityProps, onClick: () => void }> = (props) => {
  const [open, setOpen] = React.useState<boolean>(false);
  const handleClose = () => { 
    setOpen(false) 
    //props.onClick();
  };
  const handleOpen = () => {
    setOpen(true);
  }
  const Composer: React.FC< {onClose: () => void}> = open === false ? () => (<></>) : props.data.composer;
  return (
    <>
      <Composer onClose={handleClose}/>
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
          {activities.map((activity, index) => (<ActivitiesViewItem key={index} data={activity} onClick={handleComposeSelectClose}/>))}
        </MenuList>
      </EveliShellCompose>
      {migrationsDialogOpen && <MigrationComposer onClose={() => setMigrationsDialogOpen(false)} />}

      <EveliShellExplorer>
        <EveliLogo />

        <Button startIcon={<CreateOutlinedIcon />}
          className={classes.composeButton}
          onClick={handleComposeSelectClick}>
          {intl.formatMessage({ id: 'menu.compose' })}
        </Button>

        <Button variant={activeItem?.type === 'ARTICLES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<MenuBookOutlinedIcon />}
          onClick={() => onNav({ type: 'ARTICLES' })}>
          {intl.formatMessage({ id: 'menu.articles' })}
        </Button>

        <Button variant={activeItem?.type === 'SERVICES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<AccountTreeOutlinedIcon />}
          onClick={() => onNav({type: 'SERVICES'})}>
          {intl.formatMessage({ id: 'menu.services' })}
        </Button>

        <Button variant={activeItem?.type === 'LINKS' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<InsertLinkOutlinedIcon />}
          onClick={() => onNav({ type: 'LINKS'})}>
          {intl.formatMessage({ id: 'menu.links' })}
        </Button>

        <Button variant={activeItem?.type === 'LOCALES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<TranslateOutlinedIcon />}
          onClick={() => onNav({type: 'LOCALES'})}>
          {intl.formatMessage({ id: 'menu.locales' })}
        </Button>

        <Button variant={activeItem?.type === 'TEMPLATES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<FormatShapesOutlinedIcon />}
          onClick={() => onNav({type: 'TEMPLATES'})}>
          {intl.formatMessage({ id: 'menu.templates' })}
        </Button>

        <Button variant={activeItem?.type === 'MIGRATIONS' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<UploadFileOutlinedIcon />}
          onClick={() => setMigrationsDialogOpen(true)}>
          {intl.formatMessage({ id: 'menu.migrations' })}
        </Button>

        <Button variant={activeItem?.type === 'RELEASES' ? 'explorerActive' : 'explorerInactive'}
          startIcon={<NewReleasesOutlinedIcon />}
          onClick={() => onNav({type: 'RELEASES'})}>
          {intl.formatMessage({ id: 'menu.releases' })}
        </Button>

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


