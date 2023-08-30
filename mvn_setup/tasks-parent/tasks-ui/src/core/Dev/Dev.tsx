import React from 'react';

import { Box, Divider, Button } from '@mui/material';
import TestTask from './test_task_1.json';
import DatePicker from '../DatePicker';
import Checklist from '../Checklist';
import { demoChecklist } from '../Checklist/checklist-demo';
import TeamSpaceDevLayout from '../TeamSpace-dev';
import TaskEditDialog from 'core/TaskEdit';

const Dev: React.FC = () => {

  const [open, setOpen] = React.useState(false);
  const [startDate, setStartDate] = React.useState<Date | string | undefined>();
  const [dueDate, setDueDate] = React.useState<Date | string | undefined>();

  return (
    <Box sx={{ width: '100%', p: 1 }}>
      <Box sx={{ mb: 5 }}>TEAM SPACE</Box>
      <TeamSpaceDevLayout />

      <Box sx={{ mb: 5 }} />

      <Box>COMPONENT 1 create task preview</Box>
      <Divider />

      <Divider />
      <Box>Edit task dialog</Box>
      <Button variant='contained' onClick={() => setOpen(true)}>Open dialog</Button>
      <TaskEditDialog onClose={() => setOpen(false)} open={open} task={TestTask as any} />

      <Divider sx={{ my: 2 }} />
      <DatePicker startDate={startDate} setStartDate={setStartDate} dueDate={dueDate} setDueDate={setDueDate} />

      <Divider sx={{ my: 2 }} />
      <Checklist value={demoChecklist} />

    </Box>);
}

export { Dev };
