import React from 'react';

import { Divider, List } from '@mui/material';
import { Topic } from './Topic';
import Portal from '../../';


interface TopicsProps {

}

const Topics: React.FC<TopicsProps> = () => {
  const { site } = Portal.useSite();
  const topics = React.useMemo(() => {
    console.log("Rendering the topics");
    return Object.values(site?.topics ? site?.topics : {})
      .filter(topic => !topic.parent)
      .sort((a, b) => a.id.localeCompare(b.id))
      .map((topic, index) => (
        <React.Fragment key={index}>
          <Topic value={topic} />
          <Divider />
        </React.Fragment>
      ))
      ;

  }, [site]);

  if (!site) {
    return null;
  }


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
