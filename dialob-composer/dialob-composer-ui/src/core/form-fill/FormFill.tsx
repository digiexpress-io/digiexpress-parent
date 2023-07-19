import React from 'react';
import { Box } from '@mui/material';

import Burger from '@the-wrench-io/react-burger';

import { Client, Composer } from '../context';
import { PreviewContextDialog } from './PreviewContextDialog';
import View from './view/FormFill';


const FormFill: React.FC<Client.Entity> = (props) => {
  const { session, actions, service } = Composer.useComposer();
  const { site } = session;
  const form = props as Client.Form
  const formId = form.id;
  const [start, setStart] = React.useState(false);
  const [fill, setFill] = React.useState<{id: string, url: string}>();
  const [ctx, setCtx] = React.useState<Record<string, string>>({})

  React.useEffect(() => {
    if (start) {
      return;
    }


  }, [formId, service, start])

  const doc = site.forms[formId];

  const handleCreateSession = () => {
    service.create().fill({ formId, contextValues: ctx, language: "en"})
      .then(data => {
        setStart(true);
        setFill(Object.assign({}, data, {url: service.config.url + "/sessions" }))
        console.log("data", data)
      });
  } 

  const handleCtx = (newValue: { key: string, value?: string }) => {
    const next = Object.assign({}, ctx);
    next[newValue.key] = newValue.value as any
    setCtx(next);
  }
  
  return (<Box height="100%">
    {/** context variables and start new session */}
    {start ? null : (
      <>
        <PreviewContextDialog form={doc} ctx={ctx} onChange={handleCtx} />
        <Burger.PrimaryButton onClick={handleCreateSession} label={"form.fill.create"} />
      </>
    )}

    {/** dialob fill */}
    {!fill ? null : (
      <View sessionId={fill.id} sessionUrl={fill.url} onAttachments={(files: FileList) => {
        return {} as any
      }}/>
    )}

  </Box>);
}

export { FormFill };
