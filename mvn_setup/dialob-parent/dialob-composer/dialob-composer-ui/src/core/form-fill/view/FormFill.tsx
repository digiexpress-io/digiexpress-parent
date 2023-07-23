import React from 'react';
import { createStyles, makeStyles } from '@mui/styles';

import { Theme } from '@mui/material';
import { Alert, AlertTitle } from '@mui/lab';
import { FormattedMessage, useIntl } from 'react-intl';

import { ConfigContext, BreadCrumbs } from '@dialob/fill-material';
import { Session } from '@dialob/fill-react';
import DialobFill, { Config, Session as DialobSession, FillError } from '@dialob/fill-api';

import { DefaultView } from './DefaultView';
import { FormItem } from './FormItem';
import { FillMarkdown } from './FillMarkdown';


const useStyles = makeStyles((_theme: Theme) =>
  createStyles({
    completeButton: {
      marginBottom: 70
    }
  }),
);


interface FormFillProps {
  sessionId: string;
  sessionUrl: string;
  onAttachments: (files: FileList) => Promise<void>
}

const RenderErrors: React.FC<{ errors: FillError[] }> = ({ errors }) => {
  const result = errors.filter(e => e.code !== "REQUIRED");
  if (result.length === 0) {
    return null;
  }
  return <ul>{result.map((e, i) => <li key={i}>{e.description}</li>)}</ul>;
};



const DIALOB_CONFIG = {
  errors: (items: FillError[]) => <RenderErrors errors={items} />,
  description: (text: string) => <FillMarkdown text={text} />,
  breadCrumbs: (items: string[], canNavigate: boolean, activeItem?: string,) => 
    <BreadCrumbs items={items} canNavigate={canNavigate} activeItem={activeItem} />
}


const FormFill: React.FC<FormFillProps> = ({ sessionId, sessionUrl, onAttachments }) => {
  const intl = useIntl();
  const classes = useStyles();

  const [session, setSession] = React.useState<DialobSession | null>(null);
  const [complete, setComplete] = React.useState(false);
  const [error, setError] = React.useState(false);

  React.useEffect(() => {
    if (!sessionId || !sessionUrl) {
      return;
    }
    const dialobConfig: Config = { endpoint: sessionUrl, transport: { mode: 'rest' } };
    const s = DialobFill.newSession(sessionId, dialobConfig);
    s.on('error', (type, error) => setError(true));

    setSession(s);
  }, [sessionId, sessionUrl]);


  const handleClose = () => {
    console.log("Form debug close");
  }

  const handleOnComplete = () => {
    setComplete(true);
  }


  return (
    <>
      {
        error ?
          <Alert severity='error' elevation={1}>
            <AlertTitle><FormattedMessage id='error.title' /></AlertTitle>
            <FormattedMessage id='error.dialob.session' />
          </Alert>
          :
          session && (
            <ConfigContext.Provider value={DIALOB_CONFIG}>
              <Session key={session.id} session={session} locale={intl.locale} {... {
                children: (<DefaultView onComplete={handleOnComplete}>
                  {items => items.map(id => (<FormItem id={id} key={id} onAttachments={onAttachments} />))}
                </DefaultView>)
              }} />
            </ConfigContext.Provider>
          )
      }
    </>
  );
}


export default FormFill;

