import React from 'react';

import { IconButton, Typography } from '@mui/material';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import { FormattedMessage } from 'react-intl';

import { EveliShellMiniBarClassName, EveliShellMiniBarRoot } from '../eveli-shell/useUtilityClasses';
import { useWrenchNav } from '../wrench-nav';


export const Toolbar: React.FC<{}> = () => {
  const { onNav } = useWrenchNav();

  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName}>
      <div>
        <IconButton onClick={(_event) => onNav({ type: 'ACTIVITIES' })}><DashboardCustomizeOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.activities' /></Typography>
      </div>
    </EveliShellMiniBarRoot>

  );
}