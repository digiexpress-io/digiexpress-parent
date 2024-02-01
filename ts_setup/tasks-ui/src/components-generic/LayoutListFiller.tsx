import React from 'react';
import { Typography, Alert } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { LayoutListItem } from './LayoutListItem';


export const LayoutListFiller: React.FC<{ value: { emptyRows: number, entries: any[]} }> = ({ value }) => {
  if (value.entries.length === 0) {
    return (<Alert sx={{ m: 2 }} severity='info'>
      <Typography><FormattedMessage id='core.myWork.alert.entries.none' /></Typography>
    </Alert>);
  }
  const result: React.ReactNode[] = []
  for (let index = 0; index < value.emptyRows; index++) {
    result.push(<LayoutListItem active={false} key={index} index={value.entries.length + index} onClick={() => { }} children="" />)
  }

  return <>{result}</>
}
