import React from 'react';
import { List, MenuItem, Box, Button, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Client from 'client';
import Context from 'context';
import { useMockPopover } from 'taskcomponents/TaskTable/MockPopover';

const repoColors = Context.RepoTypePalette;
const repoOptions: Client.RepoType[] = ['DIALOB', 'STENCIL', 'TASKS', 'WRENCH'];

function getActiveColor(currentlyShowing: Client.RepoType, priority: Client.RepoType): string {
  const selectedItemColor = Context.StatusPalette.IN_PROGRESS;
  const color = priority === currentlyShowing ? selectedItemColor : "unset";
  return color;
}

const ProjectRepoType: React.FC<{
  project: { repoType: Client.RepoType },
  onChange: (newRepoType: Client.RepoType) => Promise<void>
}> = ({ project, onChange }) => {
  const repoType = project.repoType;
  const Popover = useMockPopover();


  function handleChangeRepoType(newRepoType: Client.RepoType) {
    onChange(newRepoType).then(() => Popover.onClose());
  }


  return (
    <Box>
      <Button color='inherit' onClick={Popover.onClick} sx={{ textTransform: 'none' }}>
        <Typography><FormattedMessage id={'project.repoType.' + repoType} /></Typography>
      </Button>
      <Popover.Delegate onClose={Popover.onClose}>
        <List dense sx={{ py: 0 }}>
          {repoOptions.map((option: Client.RepoType) => (
            <MenuItem key={option} onClick={() => handleChangeRepoType(option)} sx={{ display: "flex", pl: 0, py: 0 }}>
              <Box sx={{ width: 8, height: 40, backgroundColor: repoColors[option] }} />
              <Box sx={{ width: 8, height: 8, borderRadius: "50%", mx: 2, backgroundColor: getActiveColor(option, repoType) }} />
              <Typography><FormattedMessage id={`project.repoType.${option}`} /></Typography>
            </MenuItem>
          ))}
        </List>
      </Popover.Delegate>
    </Box>
  );
}

export default ProjectRepoType;