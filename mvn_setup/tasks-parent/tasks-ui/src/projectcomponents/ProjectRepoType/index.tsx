import React from 'react';
import { List, MenuItem, Box, Button, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import Client from 'client';
import { ProjectDescriptor } from 'projectdescriptor';
import Context from 'context';
import { useMockPopover } from 'taskcomponents/TaskTable/MockPopover';

const repoColors = Context.RepoTypePalette;
const priorityOptions: Client.RepoType[] = ['dialob', 'stencil', 'tasks', 'wrench'];

function getActiveColor(currentlyShowing: Client.RepoType, priority: Client.RepoType): string {
  const selectedItemColor = Context.StatusPalette.IN_PROGRESS;
  const color = priority === currentlyShowing ? selectedItemColor : "unset";
  return color;
}

const ProjectRepoType: React.FC<{
  task: ProjectDescriptor,
  onChange: (command: Client.ChangeRepoType) => Promise<void>
}> = ({ task, onChange }) => {
  const priority = task.repoType;
  const Popover = useMockPopover();

  function handlePriorityChange(newPriority: Client.RepoType) {
    const command: Client.ChangeRepoType = {
      commandType: 'ChangeRepoType',
      repoType: newPriority,
      projectId: task.id
    }
    onChange(command).then(() => Popover.onClose());
  }


  return (
    <Box>
      <Button color='inherit' onClick={Popover.onClick} sx={{ textTransform: 'none' }}>
        <Typography><FormattedMessage id={'projects.repoType.' + priority} /></Typography>
      </Button>
      <Popover.Delegate onClose={Popover.onClose}>
        <List dense sx={{ py: 0 }}>
          {priorityOptions.map((option: Client.RepoType) => (
            <MenuItem key={option} onClick={() => handlePriorityChange(option)} sx={{ display: "flex", pl: 0, py: 0 }}>
              <Box sx={{ width: 8, height: 40, backgroundColor: repoColors[option] }} />
              <Box sx={{ width: 8, height: 8, borderRadius: "50%", mx: 2, backgroundColor: getActiveColor(option, priority) }} />
              <Typography><FormattedMessage id={`task.priority.${option}`} /></Typography>
            </MenuItem>
          ))}
        </List>
      </Popover.Delegate>
    </Box>
  );
}

export default ProjectRepoType;