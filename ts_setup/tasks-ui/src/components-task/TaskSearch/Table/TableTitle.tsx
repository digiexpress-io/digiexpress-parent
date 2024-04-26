
import React from 'react';
import { Button, Typography } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import { useAvatar } from 'descriptor-avatar';
import { _nobody_, Palette, TaskStatus, TaskPriority } from 'descriptor-task';

import { GroupByTypes } from '../TableContext';


const TitleButtonOwner: React.FC<{ groupType: GroupByTypes, classifierValue: string }> = ({ groupType, classifierValue }) => {
  const intl = useIntl();
  const avatar = useAvatar(classifierValue);
  const backgroundColor = avatar?.colorCode;

  return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      {classifierValue === _nobody_ ? intl.formatMessage({ id: classifierValue }) : classifierValue}
  </Button>);
}

const TitleButtonRole: React.FC<{ groupType: GroupByTypes, classifierValue: string }> = ({ groupType, classifierValue }) => {
  const intl = useIntl();
  const avatar = useAvatar(classifierValue);
  const backgroundColor = avatar?.colorCode;

  return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
  {classifierValue === _nobody_ ? intl.formatMessage({ id: classifierValue }) : classifierValue}
</Button>);
}


const sx = { borderRadius: '8px 8px 0px 0px', boxShadow: "unset", fontWeight: 'bolder' };
const TitleButton: React.FC<{ groupByType: GroupByTypes, classifierValue: string }> = ({ groupByType, classifierValue }) => {

  if (!groupByType) {
    return (<Button color="primary" variant="contained" sx={sx}>Contained</Button>);
  }

  if (groupByType === 'owners') {
    return <TitleButtonOwner groupType={groupByType} classifierValue={classifierValue} />;
  } else if (groupByType === 'status') {
    
    const backgroundColor = Palette.status[classifierValue as TaskStatus];
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`tasktable.header.spotlight.status.${classifierValue}`} />
    </Button>);
  } else if (groupByType === 'priority') {
    const backgroundColor = Palette.priority[classifierValue as TaskPriority];
    return (<Button variant="contained" sx={{ ...sx, backgroundColor }}>
      <FormattedMessage id={`tasktable.header.spotlight.priority.${classifierValue}`} />
    </Button>);
  } else if (groupByType === 'roles') {
    return (<TitleButtonRole groupType={groupByType} classifierValue={classifierValue} />);
  }

  return (<Button color="primary" variant="contained" sx={sx}>
    <FormattedMessage id={`tasktable.header.spotlight.no_group`} />
  </Button>);
}

const Title: React.FC<{ groupByType: GroupByTypes, classifierValue: string, groupCount: number }> = ({ groupByType, groupCount, classifierValue }) => {

  return (<>
    <TitleButton groupByType={groupByType} classifierValue={classifierValue}/>
    <Typography sx={{ ml: 1 }} variant='caption'><FormattedMessage id={'core.teamSpace.taskCount'} values={{ values: groupCount }} /></Typography>
  </>)
}

export { Title };


