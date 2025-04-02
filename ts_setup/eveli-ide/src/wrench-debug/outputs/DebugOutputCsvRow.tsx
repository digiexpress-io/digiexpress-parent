import React from 'react';

import { Box, TableCell, TableRow, IconButton, Collapse } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import MonacoReact from '@monaco-editor/react';
import { FormattedMessage } from 'react-intl';

import { toYaml } from './utils';
import { HdesApi } from '@/api-wrench';

const DebugOutputCsvRow: React.FC<{ csvRow:HdesApi.CsvRow, index: string }> = ({ csvRow, index }) => {
    const [open, setOpen] = React.useState(false);
  
    return (<>
      <TableRow sx={{ '& > *': { borderBottom: 'unset' } }}>
        <TableCell>
          <IconButton size="small" onClick={() => setOpen(!open)}>
            {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
          </IconButton>
        </TableCell>
        <TableCell component="th" scope="row">
          <FormattedMessage id="debug.csv.row" values={{row: index}} />
        </TableCell>
      </TableRow>
  
      <TableRow>
        <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={2}>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <Box sx={{ margin: 1 }}>
              <MonacoReact value={toYaml(csvRow)} defaultLanguage='yaml'/>
            </Box>
          </Collapse>
        </TableCell>
      </TableRow>
    </>);
}
  
export { DebugOutputCsvRow };