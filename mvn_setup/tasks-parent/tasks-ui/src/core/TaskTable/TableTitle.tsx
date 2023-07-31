
import React from 'react';
import { Button, Typography } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import Client from '@taskclient';


const TableSubTitle: React.FC<{ values: number, message: string }> = ({ values, message }) => {

  return (<Typography sx={{ ml: 1 }} variant='caption'><FormattedMessage id={message} values={{ values }} /></Typography>)
}


const TableTitle: React.FC<{ group: Client.Group }> = ({ group }) => {
  const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset" };
  const intl = useIntl();
  if (!group) {
    return (<Button color="primary" variant="contained" sx={sx}>Contained</Button>);
  }

  if (group.type === 'myWorkType') {
    const backgroundColor = group.color;
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`core.myWork.myWorkTaskTable.header.spotlight.${group.id}`} />
    </Button>);
  } else if (group.type === 'status') {
    const backgroundColor = group.color;
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`tasktable.header.spotlight.status.${group.id}`} />
    </Button>);
  } else if (group.type === 'priority') {
    const backgroundColor = group.color;
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`tasktable.header.spotlight.priority.${group.id}`} />
    </Button>);
  } else if (group.type === 'owners' || group.type === 'roles') {
    const backgroundColor = group.color;
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      {group.id === Client._nobody_ ? intl.formatMessage({ id: group.id }) : group.id}
    </Button>);
  }

  return (<Button color="primary" variant="contained" sx={sx}>
    <FormattedMessage id={`tasktable.header.spotlight.no_group`} />
  </Button>);
}

export {TableSubTitle, TableTitle};


