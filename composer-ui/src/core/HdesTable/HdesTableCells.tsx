import React from 'react';
import { Box, Typography } from '@mui/material';
import DeClient from '@declient';


interface CellProps {
  tree: DeClient.HdesTree,
  row: DeClient.HdesBodyEntity
  width: string,
}


function getName(row: DeClient.HdesBodyEntity): string {
  const { bodyType, name } = row;
  switch(bodyType) {
  case 'DT': 
  case 'FLOW': 
  case 'FLOW_TASK': 
  case 'TAG': 
  }
  
  return name;
}

const AssetName: React.FC<CellProps> = ({ row, width, tree }) => {
  return (<Box display='flex'>
    <Box alignSelf="center"><Typography noWrap={true} fontSize="13px" fontWeight="400" width={width}>{row.name}</Typography></Box>
  </Box>);
}


const AssetType: React.FC<CellProps> = ({ row, width, tree }) => {
  return (<Box display='flex'>
    <Box alignSelf="center"><Typography noWrap={true} fontSize="13px" fontWeight="400" width={width}>{row.bodyType}</Typography></Box>
  </Box>);
}


export type { CellProps }
export { AssetName, AssetType };

