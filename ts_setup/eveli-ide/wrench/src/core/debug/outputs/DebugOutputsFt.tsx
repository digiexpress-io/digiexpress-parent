import React from 'react';

import { Box, TableCell, TableRow, IconButton, Collapse } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import { FormattedMessage } from 'react-intl';

import { Client } from '../../context';

import CodeEditor from '../../../code-editor';
import { toYaml } from './utils'


const DebugOutputsFt: React.FC<{ debug: Client.ServiceResult }> = ({ debug }) => {
  const [expanded, setExpanded] = React.useState(false);
  
  return (<>
    <TableRow sx={{ '& > *': { borderBottom: 'unset' } }}>
      <TableCell>
        <IconButton size="small" onClick={() => setExpanded(!expanded)}>
          {expanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
        </IconButton>
      </TableCell>
      <TableCell component="th" scope="row">
        <FormattedMessage id="debug.asset.execute.outputs.service" />
      </TableCell>
    </TableRow>

    <TableRow sx={expanded ? undefined : { visibility: "hidden" }}>
      <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={2}>
        <Collapse in={expanded} timeout="auto" unmountOnExit>
          <Box sx={{ margin: 1 }}>
            <CodeEditor id="debug-output" mode="yaml" src={toYaml(debug.value)} onChange={(value) => { console.log(value); }} />
          </Box>
        </Collapse>
      </TableCell>
    </TableRow>
  </>);
}

export { DebugOutputsFt };