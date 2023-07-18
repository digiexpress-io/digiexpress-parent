import React from 'react';
import {
  Accordion, AccordionSummary, AccordionDetails, Typography, useTheme, Theme,
  Table, TableCell, TableContainer, TableHead, TableRow, TableBody, Paper
} from '@material-ui/core';
import { createStyles, makeStyles } from '@material-ui/styles';

import ExpandMoreIcon from '@material-ui/icons/ExpandMore';

import { FormattedMessage } from 'react-intl';
import { CodeEditor } from '../deps';
import { Hdes } from '../deps';
import { useContext, Session } from './context';


type Rule = {
  name: string,
  type: string,
  rule: string | null | undefined,
  input: string | null | undefined,
};
type RuleRow = { order: number, rules: Rule[], outputs?: Object };


const createRejections = (debug: Hdes.DebugAPI.DtDebug): RuleRow[] => {
  if (debug.rejections.length === 0) {
    return [];
  }
  const result: RuleRow[] = [];
  const names = debug.rejections[0].context;

  for (const rejection of debug.rejections) {
    const ruleRow: RuleRow = { order: rejection.node.order, rules: [] };
    result.push(ruleRow);

    for (const name of names) {
      const node = rejection.expressions[name.key.name];
      const rule: Rule = {
        name: name.key.name,
        type: name.key.valueType,
        input: name.value ? name.value as string : null,
        rule: node ? node.src : null,
      }
      ruleRow.rules.push(rule);
    }
  }
  return result;
}

const createMatches = (debug: Hdes.DebugAPI.DtDebug): RuleRow[] => {

  if (debug.matches.length === 0) {
    return [];
  }

  const result: RuleRow[] = [];
  const names = debug.matches[0].context;
  for (const match of debug.matches) {


    const outputs = debug.outputs.filter(o => o.order === match.node.order)[0].values;
    const ruleRow: RuleRow = { order: match.node.order, rules: [], outputs };
    result.push(ruleRow);

    for (const name of names) {
      const node = match.expressions[name.key.name];
      const rule: Rule = {
        name: name.key.name,
        type: name.key.valueType,
        input: name.value ? name.value as string : null,
        rule: node ? node.src : null,
      }
      ruleRow.rules.push(rule);
    }
  }
  return result;
}

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    order: {
      maxWidth: "30px"
    },
  }),
);


const DebugOutputsDt: React.FC<{}> = () => {

  const theme = useTheme();
  const classes = useStyles();
  const context = useContext();
  
  const active: Session.Active = context.active as Session.Active;
  const debug: Hdes.DebugAPI.DtDebug = active.debug.output.service as Hdes.DebugAPI.DtDebug;
  const [rejects, setRejects] = React.useState(debug.outputs.length < 1);
  const rejections = createRejections(debug);
  const matches = createMatches(debug);

  return (<>
    <Accordion expanded={context.session.outputs} onChange={() => context.actions.handleOutputs(!context.session.outputs)}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Typography color="primary" >
          <FormattedMessage id={"debug.asset.execute.outputs.dt.json"} />
        </Typography>
      </AccordionSummary>
      <AccordionDetails>
        <CodeEditor.Provider mode="json" theme={theme.palette.mode === 'dark' ? 'dark' : 'light'} value={JSON.stringify(matches.map(r => r.outputs), null, 2)} onCommands={() => { }} />
      </AccordionDetails>
    </Accordion>

    <Accordion expanded={rejects} onChange={() => setRejects(!rejects)}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Typography color="primary">
          <FormattedMessage id="debug.asset.execute.outputs.dt.rejects" />
        </Typography>
      </AccordionSummary>
      <AccordionDetails>
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell className={classes.order}><FormattedMessage id="debug.asset.execute.outputs.dt.rejects.order" /></TableCell>
                {rejections.length > 0 && rejections[0].rules.map((r, index) => (<TableCell key={index} align="left">{r.name}</TableCell>))}
              </TableRow>
            </TableHead>
            <TableBody>
              {rejections.map((row, index) => (
                <TableRow key={index} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                  <TableCell className={classes.order} component="th" scope="row">{row.order}</TableCell>
                  {
                    row.rules.map((input, index) => (<TableCell align="left" key={index}>{`${input.input} ${input.rule}`}</TableCell>))
                  }
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </AccordionDetails>
    </Accordion>
  </>);
}

export { DebugOutputsDt };


