import React from 'react';

import { Box, TableCell, TableRow, IconButton, Collapse } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import { FormattedMessage } from 'react-intl';

import { HdesApi as Client } from '../../client';

import CodeEditor from '../../../code-editor';
import { toYaml } from './utils'

const DebugStep: React.FC<{ debug: Client.FlowResultLog }> = ({ debug }) => {
  const [expanded, setExpanded] = React.useState(false);

  return (<>
    <TableRow sx={{ '& > *': { borderBottom: 'unset' } }} key={`${debug.id}-summary`}>
      <TableCell>
        <IconButton size="small" onClick={() => setExpanded(!expanded)}>
          {expanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
        </IconButton>
      </TableCell>
      <TableCell component="th" scope="row">
        <FormattedMessage id="debug.asset.execute.outputs.flow.step" values={{ name: debug.stepId, status: debug.status}}/>
      </TableCell>
    </TableRow>

    <TableRow key={`${debug.id}-details`} sx={expanded ? undefined : { visibility: "hidden" }}>
      <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={2}>
        <Collapse in={expanded} timeout="auto" unmountOnExit>
          <Box sx={{ margin: 1 }}>
            <CodeEditor id="debug-input" mode="yaml" src={toYaml(debug)} onChange={(value) => { console.log(value); }} />
          </Box>
        </Collapse>
      </TableCell>
    </TableRow>
  </>);
}

const DebugOutputsFl: React.FC<{ debug: Client.FlowResult }> = ({ debug }) => {
  return (<>{debug.logs.map(e => <DebugStep key={`${e.id}-step`} debug={e} />)}</>);
}

export { DebugOutputsFl };