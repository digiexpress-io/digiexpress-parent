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

import { useIntl } from 'react-intl';

import { useUtilityClasses } from '../burger/eveli-shell/useUtilityClasses';

import logo from '../uiDev/logoLifeDigitalDark.svg';
import { MigrationComposer } from './migration';
import { useStencilNav } from './nav';

import * as Burger from '@/burger';


const Secondary: React.FC<{}> = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { activeItem, onNav } = useStencilNav();
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
      <Burger.EveliShellCompose open={!!anchorEl} anchorEl={anchorEl} onClose={handleComposeSelectClose}>
        <MenuList dense>
          <MenuItem onClick={handleComposeSelectClose}>
            <ListItemText>Article</ListItemText>
          </MenuItem>
          <MenuItem onClick={handleComposeSelectClose}>
            <ListItemText>Page</ListItemText>
          </MenuItem>
          <MenuItem onClick={handleComposeSelectClose}>
            <ListItemText>Service</ListItemText>
          </MenuItem>
          <MenuItem onClick={handleComposeSelectClose}>
            <ListItemText>Link</ListItemText>
          </MenuItem>
          <MenuItem onClick={handleComposeSelectClose}>
            <ListItemText>Locale</ListItemText>
          </MenuItem>
          <MenuItem onClick={handleComposeSelectClose}>
            <ListItemText>Migration</ListItemText>
          </MenuItem>
          <MenuItem onClick={handleComposeSelectClose}>
            <ListItemText>Template</ListItemText>
          </MenuItem>
        </MenuList>
      </Burger.EveliShellCompose>
      {migrationsDialogOpen && <MigrationComposer onClose={() => setMigrationsDialogOpen(false)} />}

      <Burger.EveliShellExplorer>
        <div className={classes.logoContainer}>
          <img src={logo} className={classes.logo} />
        </div>

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

      </Burger.EveliShellExplorer>
    </>
  )
}
export { Secondary }


