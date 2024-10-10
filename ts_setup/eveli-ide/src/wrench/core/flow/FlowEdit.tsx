import React from 'react';
import { Box, Divider, IconButton, Tooltip } from '@mui/material';

import CodeEditor, { HintProps } from '../../code-editor';
import { Client, Composer } from '../context';
import Graph from './graph';
import { AutocompleteVisitor, FlowAstAutocomplete, AutocompleteTask } from './autocomplete';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';


type GuidedHint = (cm: CodeMirror.Editor, data: CodeMirror.Hints, cur: CodeMirror.Hint) => void;

const SticyGraph: React.FC<{ flow: Client.AstFlow, site: Client.Site }> = ({ flow, site }) => {
  
  const nav = Composer.useNav();

  return (
    <Graph flow={flow} site={site}
      onClick={() => console.log("single")}
      onDoubleClick={(id) => {
        
        let article: Client.Entity<any> = site.decisions[id];
        if(!article) {
          article = site.flows[id];
        }
        if(!article) {
          article = site.services[id];
        }
        if(article) {
          nav.handleInTab({ article })
        }
      }} 
    />
  );
}


const FlowEdit: React.FC<{ flow: Client.Entity<Client.AstFlow> }> = ({ flow }) => {
  const { session, actions, service } = Composer.useComposer();
  const { site } = session;
  const update = session.pages[flow.id];
  const src = flow.ast?.src.value;
  
  const [ast, setAst] = React.useState<Client.AstFlow | undefined>(flow.ast);
  const [guided, setGuided] = React.useState<{ cm: CodeMirror.Editor, data: CodeMirror.Hints, cur: CodeMirror.Hint, guided: FlowAstAutocomplete }>();
  const [showGraph, setShowGraph] = React.useState<boolean>(false);
  const commands = React.useMemo(() => update ? update.value : flow.source.commands, [flow, update]);
  const flowId = flow.id;
  
  React.useEffect(() => {
    service.ast(flowId, commands).then(data => setAst(data.ast));
  }, [commands, flowId, service])

  return (<Box sx={{ height: 1, display: 'flex' }}>
    {guided ? <AutocompleteTask onClose={() => setGuided(undefined)} flow={flow} {...guided} /> : undefined}
    <Tooltip title={<FormattedMessage id={showGraph ? 'flows.graph.hide' : 'flows.graph.show'} />} placement="left">
      <IconButton sx={{ top: '74px', right: '10px', position: 'absolute', zIndex: 10 }} onClick={() => setShowGraph(!showGraph)}>
        {showGraph ? <VisibilityOff /> : <Visibility />}
      </IconButton>
    </Tooltip>
    <Box sx={{ width: showGraph ? 0.7 : 1 }}>
      <CodeEditor id={flow.id} mode="yaml" src={src ? src : "#--failed-to-parse"}
        onChange={(value) => actions.handlePageUpdate(flow.id, [{ type: "SET_BODY", value }])}
        hint={(hintProps: HintProps) => {
          const { pos, emptyLine } = hintProps;
          const ac = ast ? new AutocompleteVisitor(ast, site, pos).visit() : [];
          const result: CodeMirror.Hints = { from: { line: pos.line, ch: 0 }, to: pos, list: [] };
          for (const src of ac) {
            const hint: GuidedHint | undefined = src.guided ? (cm, data, cur) => setGuided({ cm, data, cur, guided: src }) : undefined;
            const from = src.append && !emptyLine ? { line: pos.line, ch: pos.ch } : undefined;
            const newText: string[] = [];
            if(src.append && !emptyLine) {
              newText.push("");
            } 
            newText.push(...src.value);
            result.list.push({ text: newText.join("\r\n"), displayText: src.id, from, hint })
          }
          return result;
        }}
        lint={() => {
          if (!ast || ast.messages.length === 0) {
            return [];
          }
          return ast?.messages.map(m => ({ type: m.type, line: m.line, value: m.value }));
        }} />
      </Box>
      <Divider orientation='vertical' />
      {(ast && showGraph) ? <SticyGraph flow={ast} site={site} /> : undefined}
  </Box>);
}

export { FlowEdit };
