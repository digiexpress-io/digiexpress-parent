
import React from 'react';
import { Button, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { Group } from 'descriptor-project';

const TableSubTitle: React.FC<{ values: number, message: string }> = ({ values, message }) => {

  return (<Typography sx={{ ml: 1 }} variant='caption'><FormattedMessage id={message} values={{ values }} /></Typography>)
}


const TableTitle: React.FC<{ group: Group }> = ({ group }) => {
  const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset", fontWeight: 'bolder' };

  if (!group) {
    return (<Button color="primary" variant="contained" sx={sx}>Contained</Button>);
  }

  if (group.type === 'users') {
    const backgroundColor = group.color;
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`projects.header.groupBy.repoType.${group.id}`} />
    </Button>);
  } else if (group.type === 'repoType') {
    const backgroundColor = group.color;
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`projects.header.groupBy.repoType.${group.id}`} />
    </Button>);
  }
  return (<Button color="primary" variant="contained" sx={sx}>
    <FormattedMessage id={`projects.header.spotlight.no_group`} />
  </Button>);
}

export { TableSubTitle, TableTitle };


