import React from 'react';
import { IconButton, Typography } from '@mui/material';
import { HelpOutlineOutlined as HelpOutlineOutlinedIcon } from '@mui/icons-material';
import { ListAlt as ListIcon } from '@mui/icons-material';
import { TaskOutlined as TaskOutlinedIcon } from '@mui/icons-material';
import { BuildOutlined as BuildOutlinedIcon } from '@mui/icons-material';
import { EditNoteOutlined as EditNoteOutlinedIcon } from '@mui/icons-material';
import { BeenhereOutlined as BeenhereOutlinedIcon } from '@mui/icons-material';
import { SettingsApplicationsOutlined as SettingsApplicationsOutlinedIcon } from '@mui/icons-material';
import { FolderOutlined as FolderOutlinedIcon } from '@mui/icons-material';

import { FormattedMessage } from 'react-intl';
import { useLocation, useNavigate } from '@tanstack/react-router'

import { EveliShellMiniBarClassName, EveliShellMiniBarRoot, _eveli_shell_useUtilityClasses as useUtilityClasses } from '@dxs-ts/eveli-primitives';

import { EveliLocales, EveliPermissions } from '@dxs-ts/eveli-primitives';
import { useFetch } from '@dxs-ts/envir-fetch';
import { EveliTenantFeatureEnabled } from '@dxs-ts/eveli-api';

import { InfoOutlined as InfoOutlinedIcon } from '@mui/icons-material';
import { Dialog, DialogTitle, DialogContent } from '@mui/material';

import { ToolbarBuildInfoRoot, useUtilityClasses as useBuildInfoClasses } from './useUtilityClasses';



export const Toolbar: React.FC<{}> = ({ }) => {
  const navigate = useNavigate();
  const classes = useUtilityClasses();
  const location = useLocation();

  const [buildOpen, setBuildOpen] = React.useState(false);
  const info = useFetch('worker/rest/api/version.GET', {});
  const buildClasses = useBuildInfoClasses();

  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName}>

      <EveliTenantFeatureEnabled id='EVELI_TREE_ENABLED'>
        <div> {/* divs needed to maintain IconButton styling, otherwise IconButton background stretches */}
          <IconButton {...(location.pathname.includes('filesystem') ? { disabled: true, className: classes.itemActive } : {})}
            onClick={() => navigate({
              from: '/secured/$locale',
              to: '/secured/$locale/worker/filesystem',
              search: { explorer: [] }
            })}>
            <FolderOutlinedIcon />
          </IconButton>
          <Typography {...(location.pathname.includes('filesystem') ? { className: classes.textActive } : {})}>
            <FormattedMessage id='toolbar.files' />
          </Typography>
        </div>
      </EveliTenantFeatureEnabled>

      <EveliPermissions id='NAV_TO_TASKS'>
        <div> {/* divs needed to maintain IconButton styling, otherwise IconButton background stretches */}
          <IconButton {...(location.pathname.includes('tasks') ? { disabled: true, className: classes.itemActive } : {})}
            onClick={() => navigate({
              from: '/secured/$locale',
              to: '/secured/$locale/worker/tasks',
              search: { explorer: [] }
            })}>
            <TaskOutlinedIcon />
          </IconButton>
          <Typography {...(location.pathname.includes('tasks') ? { className: classes.textActive } : {})}>
            <FormattedMessage id='toolbar.tasks' />
          </Typography>
        </div>
      </EveliPermissions>

      <EveliTenantFeatureEnabled id='WRENCH_ENABLED'>
        <EveliPermissions id='NAV_TO_WRENCH'>
          <div>
            <IconButton
              {...(location.pathname.includes('wrench') ? { disabled: false, className: classes.itemActive } : {})}
              onClick={() => navigate({
                from: '/secured/$locale',
                to: '/secured/$locale/assets/wrench',
                search: { explorer: [] }
              })}>
              <BuildOutlinedIcon />
            </IconButton>
            <Typography><FormattedMessage id='toolbar.wrench' /></Typography>
          </div>
        </EveliPermissions>
      </EveliTenantFeatureEnabled>

      <EveliTenantFeatureEnabled id='STENCIL_ENABLED'>
        <EveliPermissions id='NAV_TO_STENCIL'>
          <div>
            <IconButton
              {...(location.pathname.includes('stencil') ? { disabled: false, className: classes.itemActive } : {})}
              onClick={() => navigate({
                from: '/secured/$locale',
                to: '/secured/$locale/assets/stencil',
                search: { explorer: [] }
              })}>
              <EditNoteOutlinedIcon />
            </IconButton>
            <Typography><FormattedMessage id='toolbar.stencil' /></Typography>
          </div>
        </EveliPermissions>
      </EveliTenantFeatureEnabled>

      <EveliTenantFeatureEnabled id='DIALOB_ENABLED'>
        <EveliPermissions id='NAV_TO_DIALOB'>
          <div>
            <IconButton {...(location.pathname.endsWith('forms') ? { disabled: true, className: classes.itemActive } : {})}
              onClick={() => navigate({
                from: '/secured/$locale',
                to: '/secured/$locale/assets/forms'
              })}>
              <ListIcon />
            </IconButton>
            <Typography {...(location.pathname.endsWith('forms') ? { className: classes.textActive } : {})
            }>
              <FormattedMessage id='menu.forms' />
            </Typography>
          </div>
        </EveliPermissions>
      </EveliTenantFeatureEnabled>

      <EveliTenantFeatureEnabled id='TAGOMI_ENABLED'>
        <EveliPermissions id='NAV_TO_TAGOMI'>
          <div>
            <IconButton
              {...(location.pathname.includes('tagomi') ? { disabled: true, className: classes.itemActive } : {})}
              onClick={() => navigate({
                from: '/secured/$locale',
                to: '/secured/$locale/assets/tagomi',
                search: { explorer: [] }
              })}>
              <EditNoteOutlinedIcon />
            </IconButton>
            <Typography><FormattedMessage id='toolbar.tagomi' /></Typography>
          </div>
        </EveliPermissions>
      </EveliTenantFeatureEnabled>


      <EveliTenantFeatureEnabled id='COCKPITS_ENABLED'>
        <div>
          <IconButton
            {...(location.pathname.includes('cockpits') ? { disabled: true, className: classes.itemActive } : {})}
            onClick={() => navigate({
              from: '/secured/$locale',
              to: '/secured/$locale/worker/cockpits',
              search: { explorer: [] }
            })}>
            <SettingsApplicationsOutlinedIcon />
          </IconButton>
          <Typography><FormattedMessage id='toolbar.cockpits' /></Typography>
        </div>
      </EveliTenantFeatureEnabled>

      <EveliPermissions id='NAV_TO_RELEASES'>
        <div>
          <IconButton {...(location.pathname.endsWith('publications') ? { disabled: true, className: classes.itemActive } : {})}
            onClick={() => navigate({
              from: '/secured/$locale',
              to: '/secured/$locale/publications'
            })}>
            <BeenhereOutlinedIcon />
          </IconButton>

          <Typography {...(location.pathname.endsWith('publications') ? { className: classes.textActive } : {})}>
            <FormattedMessage id='menu.publications' />
          </Typography>
        </div>
      </EveliPermissions>



      <div>
        <IconButton onClick={() => window.open("https://github.com/digiexpress-io/digiexpress-parent/wiki", "_blank")}>
          <HelpOutlineOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.help' /></Typography>
      </div>


      <div>
        <IconButton onClick={() => setBuildOpen(true)}>
          <InfoOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.about' /></Typography>
      </div>

      <EveliLocales />

      <Dialog open={buildOpen} onClose={() => setBuildOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          <FormattedMessage id='dialog.about.title' />
        </DialogTitle>

        <DialogContent>
          <ToolbarBuildInfoRoot className={buildClasses.root}>
            <Typography variant='body2' >
              <FormattedMessage id="dialog.about.preamble" />
            </Typography>

            <Typography variant='caption'>
              <FormattedMessage id="activities.version.composer" values={{ version: info?.frontend.version, date: info?.frontend.built }} />
            </Typography>

            <Typography variant='caption' >
              <FormattedMessage id="activities.version.core" values={{ version: info?.backend.version, date: info?.backend.built }} />
            </Typography>

            <Typography variant='caption' sx={{ mt: 2 }}>
              <FormattedMessage id="dialog.about.trademark" />
            </Typography>
          </ToolbarBuildInfoRoot>
        </DialogContent>
      </Dialog>

    </EveliShellMiniBarRoot>

  );
}



