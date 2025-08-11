import React from 'react';

import { Box, TableCell, TableRow, IconButton, Collapse } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import { FormattedMessage } from 'react-intl';
import MonacoReact from '@monaco-editor/react';
import { HdesApi } from '@dxs-ts/wrench-api';

import { toYaml } from './outputs/utils'

const DebugError: React.FC<{ error: HdesApi.StoreError }> = ({ error }) => {
  const [expanded, setExpanded] = React.useState(false);

  return (<>
    <TableRow sx={{ '& > *': { borderBottom: 'unset' } }} key="error">
      <TableCell>
        <IconButton size="small" onClick={() => setExpanded(!expanded)}>
          {expanded ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
        </IconButton>
      </TableCell>
      <TableCell component="th" scope="row">
        <FormattedMessage id="debug.asset.execute.errors" />
      </TableCell>
    </TableRow>

    <TableRow key={"error-details"} sx={expanded ? undefined : { visibility: "hidden" }}>
      <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={2}>
        <Collapse in={expanded} timeout="auto" unmountOnExit>
          <Box sx={{ margin: 1 }}>
            <MonacoReact key="debug-input" value={toYaml(error)} defaultLanguage='yaml'/>
          </Box>
        </Collapse>
      </TableCell>
    </TableRow>
  </>);
}


export { DebugError };

