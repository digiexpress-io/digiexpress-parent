import React from 'react';
import { Chip, Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { StencilClient } from '@the-stencil-io/composer';
import DeClient from '@declient';


interface CellProps {
  row: StencilClient.Article
  tree: DeClient.StencilTree
  width: string,
}


const ArticleName: React.FC<CellProps> = ({ row, width, tree }) => {

  return (<Box display='flex'>
    <Box alignSelf="center"><Typography noWrap={true} fontSize="13px" fontWeight="400" width={width}>{row.body.name}</Typography></Box>
  </Box>);
}

export type { CellProps }
export { ArticleName };

