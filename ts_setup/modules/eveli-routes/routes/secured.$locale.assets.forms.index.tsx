import { createFileRoute } from '@tanstack/react-router'
import React from 'react'
import { Box, useTheme } from '@mui/system';
import { Button } from '@mui/material';
import { HelpOutlineOutlined as HelpOutlineOutlinedIcon } from '@mui/icons-material';

import { useIntl } from "react-intl";

import { DialobDashboardSmart } from '@dxs-ts/eveli-primitives';

import { EveliSetup } from '../eveli-setup';
import { EveliApp } from '../eveli-app';


export const Route = createFileRoute('/secured/$locale/assets/forms/')({
  component: Component,
})

function Component() {
  const main = MainSmart;

  return (<EveliApp main={main} secondary={Secondary} toolbar={EveliSetup.Toolbar} />)

}


const MainSmart: React.FC<{}> = () => {
  const intl = useIntl();
  const theme = useTheme();


  return (<Box sx={{ p: theme.spacing(1) }}>
    <DialobDashboardSmart />
  </Box>)
}


const Secondary: React.FC = () => {
  const intl = useIntl();
  const theme = useTheme();

  return (<Box p={theme.spacing(1)}>
    <Button variant='explorerInactive'
      startIcon={<HelpOutlineOutlinedIcon />}
      onClick={() => window.open("https://github.com/dialob/dialob-parent/wiki", "_blank")}>
      {intl.formatMessage({ id: 'menu.help' })}
    </Button>
  </Box>)
}