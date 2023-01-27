import React from 'react';
import { Box, Typography, Paper, List, ListItem, useTheme, ListItemText, ListItemAvatar, Avatar } from '@mui/material';
import LocaleIcon from '@mui/icons-material/TranslateOutlined';

import DeClient from '@declient';


const CellInfoFlow: React.FC<{
  row: DeClient.ServiceDescriptor,
  assocs: DeClient.DefStateAssocs,
  def: DeClient.DefinitionState,
  id: string
}> = ({ row, def, assocs }) => {
  const theme = useTheme();
  const workflow = assocs.getWorkflow(row.name)!;
  console.log(workflow)

  const byTopic: Record<string, DeClient.WorkflowAssocsValue[]> = {};
  for (const value of workflow.values) {
    if (!byTopic[value.topic.id]) {
      byTopic[value.topic.id] = [];
    }
    byTopic[value.topic.id].push(value);
  }
  const height = workflow.values.length > 16 ? "500px" : undefined;

  return (<Paper sx={{
    width: "700px", height, overflow: "auto",
    backgroundColor: theme.palette.explorer.main
  }}>
    <List sx={{ width: '100%' }} >
      {Object.entries(byTopic).map(([id, topics]) => (
        <ListItem key={id} sx={{ mt: 1, mb: 1 }}>
          <ListItemAvatar sx={{ alignSelf: "self-start", mt: 1 }}><Avatar><LocaleIcon /></Avatar></ListItemAvatar>
          <ListItemText
            primary={<Typography color="explorerItem.dark" fontWeight={800}>{id}</Typography>}
            secondary={topics.map(topic => (
              <Box sx={{ pl: 3 }}>
                <Typography color="explorerItem.main" fontSize="13px">{topic.locale} - {topic.topic.name} - {topic.workflow.name}</Typography>
              </Box>
            ))} />
        </ListItem>
      ))}
    </List>

  </Paper>)
};

export default CellInfoFlow;

