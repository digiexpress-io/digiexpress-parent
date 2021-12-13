import React from 'react';
import { Box } from '@mui/material';

import CodeEditor from '../../code-editor';
import { Client, Composer } from '../context';
import Graph from './graph';
import { AutocompleteVisitor } from './autocomplete/api';


const SticyGraph: React.FC<{ flow: Client.Entity<Client.AstFlow>, site: Client.Site }> = ({ flow, site }) => {
  return (<Box sx={{ top: "64px", right: "30px", position: "absolute", zIndex: "100000000000" }}>
    <Graph flow={flow} site={site}
      onClick={() => console.log("single")}
      onDoubleClick={() => console.log("double")} />
  </Box>);
}

const FlowEdit: React.FC<{ flow: Client.Entity<Client.AstFlow> }> = ({ flow }) => {
  const { session, actions, service } = Composer.useComposer();
  const { site } = session;
  const update = session.pages[flow.id];
  const [ast, setAst] = React.useState<Client.AstFlow | undefined>(flow.ast);
  const commands = React.useMemo(() => update ? update.value : flow.source.commands, [flow, update]);
  
  const hints = (pos: CodeMirror.Position, content: string, empty: boolean) => {
    const ac = ast ? new AutocompleteVisitor(ast, site, pos).visit() : [];
    const result: CodeMirror.Hints = { from: { line: pos.line, ch: 0 }, to: pos, list: [] };
    for (const src of ac) {
      result.list.push({ text: src.value.join("\r\n"), displayText: src.id, from: src.append && !empty ? { line: pos.line, ch: pos.ch } : undefined })
    }
    return result;
  };


  React.useEffect(() => {
    service.ast(flow.id, commands).then(data => setAst(data.ast));
  }, [commands])
  

  const handleChange = (value: string | undefined) => {
    actions.handlePageUpdate(flow.id, [{ type: "SET_BODY", value: value }])
  }
  const src = flow.ast?.src.value;


  return (<Box height="100%">
    {flow.ast ? <SticyGraph flow={flow} site={site} /> : undefined}
    <CodeEditor id={flow.id} mode="yaml" src={src ? src : "#--failed-to-parse"}
      onChange={handleChange}
      hint={hints} />
  </Box>);
}

export { FlowEdit };
