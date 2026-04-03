import React from 'react';
import { Box, Divider, IconButton, Tooltip } from '@mui/material';



import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import Graph from './graph';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';
import { useWrenchNav } from '../wrench-nav';
import { FlowCodeEditor } from './FlowCodeEditor';



const SticyGraph: React.FC<{ flow:HdesApi.AstFlow, site:HdesApi.Site }> = ({ flow, site }) => {
  const { onNav } = useWrenchNav();

  return (
    <Graph flow={flow} site={site}
      onClick={() => console.log("single")}
      onDoubleClick={(id) => {
        let article:HdesApi.Entity<any> = site.decisions[id];
        if(!article) {
          article = site.flows[id];
        }
        if(!article) {
          article = site.services[id];
        }
        if (article) {
          onNav({ type: 'ENTITY_EDITOR', id: article.id })
        }
      }} 
    />
  );
}


const FlowEdit: React.FC<{ flow: HdesApi.Entity<HdesApi.AstFlow> }> = ({ flow }) => {
  const { session, actions, service, site } = Composer.useComposer();
  const update = session.pages[flow.id];
  const [ast, setAst] = React.useState<HdesApi.AstFlow | undefined>(flow.ast);
  
  const [showGraph, setShowGraph] = React.useState<boolean>(true);
  const commands: string = React.useMemo(() => update ? update.value : flow.ast?.parseTree?.value ?? '', [flow, update]) as string;
  const astReqSeq = React.useRef(0);
  const flowId = flow.id;

  React.useEffect(() => {
    const reqId = ++astReqSeq.current;
    service.ast(flowId, 'FLOW', commands)
      .then(data => {
        if (reqId === astReqSeq.current) {
          setAst(data.ast);
        }
      })
      .catch(error => {
        console.error('AST request failed:', error);
      });
  
  }, [commands, flowId, service]);


  const originalState = flow.ast?.parseTree?.value;
  const updatedContent: string = update?.value as string;
  const src = updatedContent ?? originalState;
  const lintingMessages = ast?.messages ?? [];

  return (<Box sx={{ height: 1, display: 'flex' }}>
    <Tooltip title={<FormattedMessage id={showGraph ? 'flows.graph.hide' : 'flows.graph.show'} />} placement="left">
      <IconButton sx={{ top: '74px', right: '10px', position: 'absolute', zIndex: 10 }} onClick={() => setShowGraph(!showGraph)}>
        {showGraph ? <VisibilityOff /> : <Visibility />}
      </IconButton>
    </Tooltip>
    <Box sx={{ width: showGraph ? 0.7 : 1, minHeight: '900px' }}>
      <FlowCodeEditor id={flow.id} src={src ? src : "#--failed-to-parse"}
        ast={ast}
        flow={flow}
        onChange={(value) => actions.handlePageUpdate(flow.id, value)}
        messages={lintingMessages} />
      </Box>
      <Divider orientation='vertical' />
      {(ast && showGraph) ? <SticyGraph flow={ast} site={site} /> : undefined}
  </Box>);
}

export { FlowEdit };
