import React from 'react';
import { Box } from '@mui/material';
import { Content } from './Content';
import Portal from '../../';
import { PrimaryProps } from '../../context/AppAPI';

const Primary: React.FC<PrimaryProps> = ({ sx }) => {
  const view = Portal.useTopic();
  const id = view ? view.topic.id : "";
  return (
    <Box sx={Object.assign({}, sx, {p: 2})} key={id}>
      <Content />
    </Box>
  );
}


export { Primary };

