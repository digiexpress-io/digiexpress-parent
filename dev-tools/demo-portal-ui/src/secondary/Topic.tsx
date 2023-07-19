import React from 'react';
import { alpha } from "@mui/material/styles";

import { List, ListItem, ListItemText, Collapse, Box } from '@mui/material';

import ExpandMore from '@mui/icons-material/ExpandMore';
import ExpandLess from '@mui/icons-material/ExpandLess';

import Portal from '@the-stencil-io/portal';



interface TopicProps {
  value: Portal.Topic;
}

//<ListItemText secondary={sub.name} classes={{ secondary: (topic?.id === sub.id ? classes.active : undefined) }} />



const Topic: React.FC<TopicProps> = ({ value }) => {
  const { setTopic, site } = Portal.useSite();
  const [open, setOpen] = React.useState(false);


  const subTopics = Object.values(site?.topics ? site?.topics : {})
    .filter(t => t.parent === value.id)
    .map((sub, index) => (
      <ListItem key={index} button selected={true} sx={{ ml: 3 }} onClick={() => setTopic(sub)}>
        <ListItemText secondary={sub.name} />
      </ListItem>
    ));
  const length = subTopics.length;

  const onMainTopicClick = () => {
    if (!open || length === 0) {
      setTopic(value);
    }
    setOpen(!open);

  };

  const mainTopicName = length > 0 ? `${value.name} (${length})` : `${value.name}`;
  return (
    <Box whiteSpace="normal">
      <ListItem button onClick={onMainTopicClick} >
        <ListItemText primary={mainTopicName} sx={{ color: 'text.secondary' }} />
        {length === 0 ? null :
          (open ? <ExpandLess sx={{ color: 'text.secondary' }} /> : <ExpandMore sx={{ color: 'text.secondary' }} />
          )
        }
      </ListItem >

      <Collapse in={open} timeout="auto" unmountOnExit sx={{ backgroundColor: (theme) => alpha(theme.palette.secondary.main, 0.1), ml: 2 }}>
        <List component="div" disablePadding dense
          sx={{ backgroundColor: (theme) => alpha(theme.palette.secondary.main, 0.3), width: 10 }}>{subTopics}</List>
      </Collapse>
    </Box>
  );

}
export { Topic }
