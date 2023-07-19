import React from 'react';
import { Box } from '@mui/material';

import { Client, Composer } from '../context';
import { ComposerInit } from './ComposerInit';


const FormEdit: React.FC<Client.Entity> = (props) => {
  const { session, actions, service } = Composer.useComposer();
  const { site } = session;
  const revision = props as Client.FormRev
  
  const formId = revision.head;

  React.useEffect(() => {
    //service.ast(flowId, commands).then(data => setAst(data.ast));
  }, [formId, service])

  const body = React.useMemo(() => {
    console.log("FormEdit.reinit composer")
    return (<ComposerInit config={service.config} formId={formId} key={formId}/>);
  }, [formId, service])

  return (<Box height="100%">{body}</Box>);
}

export { FormEdit };
