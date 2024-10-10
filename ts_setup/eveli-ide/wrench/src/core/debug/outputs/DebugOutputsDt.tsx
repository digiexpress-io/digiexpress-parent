import React from 'react';

import { Box, TableCell, TableRow, IconButton, Collapse } from '@mui/material';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';

import { FormattedMessage } from 'react-intl';
import CodeEditor from '../../../code-editor';
import { Client } from '../../context';
import { toYaml } from './utils'

type Rule = {
  name: string,
  type: string,
  rule: string | null | undefined,
  input: string | null | undefined,
  match: boolean
};
type RuleRow = { order: number, rules: Rule[], outputs?: Object };


const createRejections = (debug: Client.DecisionResult): RuleRow[] => {
  if (debug.rejections.length === 0) {
    return [];
  }
  const result: RuleRow[] = [];
  for (const rejection of debug.rejections) {
    const ruleRow: RuleRow = { order: rejection.order, rules: [] };
    result.push(ruleRow);

    for (const accepts of rejection.accepts) {

      const rule: Rule = {
        name: accepts.headerType.name,
        type: accepts.headerType.valueType,
        input: accepts.usedValue + "",
        rule: accepts.expression,
        match: accepts.match
      }
      ruleRow.rules.push(rule);
    }
  }
  return result;
}

const createMatches = (debug: Client.DecisionResult): RuleRow[] => {

  if (debug.matches.length === 0) {
    return [];
  }
  const result: RuleRow[] = [];
  for (const match of debug.matches) {
    const outputs = {};
    const ruleRow: RuleRow = { order: match.order, rules: [], outputs };
    result.push(ruleRow);
    for (const accepts of match.accepts) {

      const rule: Rule = {
        name: accepts.headerType.name,
        type: accepts.headerType.valueType,
        input: accepts.usedValue + "",
        rule: accepts.expression,
        match: accepts.match
      }
      ruleRow.rules.push(rule);
    }
    
    for (const matchReturns of match.returns) {
      const value: string = matchReturns.usedValue + "";
      const name: string = matchReturns.headerType.name;
      outputs[name] = value;
    }
  }
  return result;
}

const DebugOutputsDt: React.FC<{ debug: Client.DecisionResult }> = ({ debug }) => {
  const [accepted, setAccepted] = React.useState(false);
  const [rejects, setRejects] = React.useState(debug.rejections.length < 1);
  const rejections = createRejections(debug);
  const matches = createMatches(debug);

  return (<>
    <TableRow sx={{ '& > *': { borderBottom: 'unset' } }}>
      <TableCell>
        <IconButton size="small" onClick={() => setAccepted(!accepted)}>
          {accepted ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
        </IconButton>
      </TableCell>
      <TableCell component="th" scope="row">
        <FormattedMessage id="debug.asset.execute.outputs.dt.json" />
      </TableCell>
    </TableRow>

    <TableRow>
      <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={2}>
        <Collapse in={accepted} timeout="auto" unmountOnExit>
          <Box sx={{ margin: 1 }}>
            <CodeEditor id="debug-input" mode="yaml" src={toYaml(matches)}
              onChange={(value) => {
                console.log(value);
              }} />
          </Box>
        </Collapse>
      </TableCell>
    </TableRow>


    <TableRow sx={{ '& > *': { borderBottom: 'unset' } }}>
      <TableCell>
        <IconButton size="small" onClick={() => setRejects(!rejects)}>
          {rejects ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
        </IconButton>
      </TableCell>
      <TableCell component="th" scope="row">
        <FormattedMessage id="debug.asset.execute.outputs.dt.rejects" />
      </TableCell>
    </TableRow>

    <TableRow>
      <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={2}>
        <Collapse in={rejects} timeout="auto" unmountOnExit>
          <Box sx={{ margin: 1 }}>
            <CodeEditor id="debug-input" mode="yaml" src={toYaml(rejections)}
              onChange={(value) => {
                console.log(value);
              }} />
          </Box>
        </Collapse>
      </TableCell>
    </TableRow>
  </>);
}

export { DebugOutputsDt };


