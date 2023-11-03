import React from 'react';

import { Divider, List } from '@mui/material';
import Portal from '@the-stencil-io/portal';
import { Topic } from './Topic';


interface TopicsProps {

}

const Topics: React.FC<TopicsProps> = () => {

  const { site } = Portal.useSite();
  
  if (!site) {
    return null;
  }

  const topics = Object.values(site.topics)
    .filter(topic => !topic.parent)
    .sort((a, b) => a.id.localeCompare(b.id))
    .map((topic, index) => (
      <React.Fragment key={index}>
        <Topic value={topic} />
        <Divider />
      </React.Fragment>
    ))

  return (
    <List component="nav" dense
      sx={{
        paddingRight: 1,
        paddingLeft: 1,
      }}
    >{topics}</List>
  );

}
export { Topics }
