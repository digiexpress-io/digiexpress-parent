import React from 'react';

import { IconButton, Typography } from '@mui/material';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import LanguageIcon from '@mui/icons-material/Language';
import SearchIcon from '@mui/icons-material/Search';
import TaskOutlinedIcon from '@mui/icons-material/TaskOutlined';
import EditNoteOutlinedIcon from '@mui/icons-material/EditNoteOutlined';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';

import { FormattedMessage, useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';

import * as Burger from '@/burger';

import { Composer } from './context';
import { EveliShellMiniBarClassName, EveliShellMiniBarRoot, useUtilityClasses } from '../../burger/eveli-shell/useUtilityClasses';
import { LocaleSelect } from '../../uiDev/LocaleSelect';
import { useNavigate } from '@tanstack/react-router';




const Toolbar: React.FC<{}> = () => {
  const navigate = useNavigate();
  const { locale } = useIntl();
  const composer = Composer.useComposer();
  const tabs = Burger.useTabs();
  const secondary = Burger.useIconbar();
  const { enqueueSnackbar } = useSnackbar();

  React.useEffect(() => tabs.handleTabAdd({ id: 'activities', label: "Activities" }), []);

  const classes = useUtilityClasses();

  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const handleLocalePopoverClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleLocalePopoverClose = () => {
    setAnchorEl(null);
  };


  const unsavedPages = Object.values(composer.session.pages).filter(p => !p.saved);
  const saveIconClassName = unsavedPages.length ? classes.unsaved : classes.itemDisabled;

  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {
    if (newValue === 'toolbar.save' && unsavedPages) {
      if (unsavedPages.length === 0) {
        return;
      }
      const active = tabs.session.tabs.length ? tabs.session.tabs[tabs.session.history.open] : undefined;

      const article = active ? composer.session.getEntity(active.id) : undefined;
      if (!article) {
        return;
      }
      const toBeSaved = unsavedPages.filter(p => !p.saved).filter(p => p.origin.id === article.id);
      if (toBeSaved.length !== 1) {
        return;
      }

      const unsavedArticlePages: Composer.PageUpdate = toBeSaved[0];
      composer.service.update(article.id, unsavedArticlePages.value).then(success => {
        composer.actions.handlePageUpdateRemove([article.id]);
        enqueueSnackbar(<FormattedMessage id="activities.assets.saveSuccess" values={{ name: article.ast?.name }} />);
        composer.actions.handleLoadSite(success);
      }).catch((error) => {

      });

    } else if (newValue === 'toolbar.activities') {
      tabs.handleTabAdd({ id: 'activities', label: "Activities" });
    } else if (newValue === 'toolbar.search') {
      secondary.handleActiveId("toolbar.search")
    } 

  };


  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName} ownerState={{ unsaved: unsavedPages.length > 0 }}>
      <LocaleSelect open={!!anchorEl} onClose={handleLocalePopoverClose} anchorEl={anchorEl} />
      <div>
        <IconButton onClick={(event) => handleChange(event, 'toolbar.activities')}><DashboardCustomizeOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.activities' /></Typography>
      </div>

      <div>
        <IconButton className={saveIconClassName} onClick={(event) => handleChange(event, 'toolbar.save')} ><SaveOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.save' /></Typography>
      </div>

      <div>
        <IconButton onClick={(event) => handleChange(event, 'toolbar.search')}><SearchIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.search' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale/assets/wrench',
          to: '/secured/$locale'
        })}>
          <TaskOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.tasks' /></Typography>
      </div>

      <div>
        <IconButton disabled className={classes.itemActive}><BuildOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.wrench' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale/assets/wrench',
          to: '/secured/$locale/assets/stencil',
          search: { explorer: ['ARTICLES'] }
        })}>
          <EditNoteOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.stencil' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => window.open("https://github.com/the-stencil-io/the-stencil-composer/wiki", "_blank")}>
          <HelpOutlineOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.help' /></Typography>
      </div>

      <div>
        <IconButton onClick={handleLocalePopoverClick}><LanguageIcon /></IconButton>
        <Typography>{locale.toLocaleUpperCase()}</Typography>
      </div>
    </EveliShellMiniBarRoot>
  );
}


export default Toolbar;