import React from 'react';
import { Chip, Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import DeClient from '@declient';


interface CellProps {
  row: DeClient.DialobFormRevisionDocument,
  tree: DeClient.DialobTree,
  width: string,
}


const DialobName: React.FC<CellProps> = ({ row, width, tree }) => {

  return (<Box display='flex'>
    <Box alignSelf="center"><Typography noWrap={true} fontSize="13px" fontWeight="400" width={width}>{row.name}</Typography></Box>
  </Box>);
}

export type { CellProps }
export { DialobName };

