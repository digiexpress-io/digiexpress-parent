import React, { useEffect, useState } from 'react';
import {
  CircularProgress, Grid, Typography, Button, Paper, Dialog,
  DialogTitle, DialogContent, DialogActions, Box
} from '@mui/material';

import AdapterDateFns from '@mui/lab/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers'

import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';

import { FormattedMessage } from 'react-intl';

import 'moment/locale/fi';
import 'moment/locale/sv';
import 'moment/locale/et';

import { useFillActions, useFillItem, useFillSession } from '@dialob/fill-react';
import { Session } from '@dialob/fill-api';
import { Questionnaire } from '@dialob/fill-material';



export interface DefaultViewProps {
  children: (itemIds: string[]) => React.ReactNode;
  onComplete?: (session: Session) => void;
};

export const DefaultView: React.FC<DefaultViewProps> = ({ children, onComplete }) => {

  const { item: questionnaire } = useFillItem('questionnaire');
  const session = useFillSession();
  //const sessionLocale = useFillLocale();
  const fillActions = useFillActions();
  const [completed, setCompleted] = useState(session.isComplete());
  const [completeConfirmationOpen, setCompleteConfirmationOpen] = useState(false);

  useEffect(() => {
    const updateListener = () => {
      setCompleted(session.isComplete());
    }
    const completeListener = (syncState: any) => {
      if (syncState === 'DONE' && session.isComplete() && onComplete) {
        onComplete(session);
      }
    }
    fillActions.on('update', updateListener);
    fillActions.on('sync', completeListener);
    return () => {
      fillActions.removeListener('update', updateListener);
      fillActions.removeListener('sync', completeListener);
    }
  }, [session, onComplete, fillActions]);

  //const locale = sessionLocale || 'en';

  const preComplete = () => {
    setCompleteConfirmationOpen(true);
  }

  console.log("default view");
  if (!session.id) {
    return (
      <>
        <div>
          <FormattedMessage id='session.not.found' />
        </div>
      </>
    )
  } else if (!questionnaire) {
    return (
      <Grid container={true} justifyItems='center' alignItems='center' style={{ minHeight: '100vh' }}>
        <Grid item>
          <CircularProgress size={50} />
        </Grid>
      </Grid>
    );
  }

  if (completed) {
    return (
      <Paper style={{ marginTop: '2em', padding: '2em' }}>
        <Typography variant='h2'><FormattedMessage id='form.completed' /></Typography>
      </Paper>
    );
  }

  return (
    <Box>
      <Grid container spacing={1}>

        {
          questionnaire.items && questionnaire.activeItem ?
            <Questionnaire>
              {questionnaire.items && children(questionnaire.items)}
            </Questionnaire>
            : null
        }
      </Grid>

      {questionnaire.allowedActions && (
        <Grid container justifyItems='space-between' spacing={3} style={{ marginTop: '1em', marginBottom: '1em' }}>
          <Grid item >
            {questionnaire.allowedActions.includes('PREVIOUS') && (
              <Button variant='contained' onClick={() => { window.scrollTo(0, 0); fillActions.previous(); }} startIcon={<ChevronLeftIcon />}>
                <FormattedMessage id='page.previous' />
              </Button>
            )}
          </Grid>
          <Grid item>
            {!questionnaire.allowedActions.includes('NEXT') && (
              <Button variant='contained' color='primary'
                disabled={!questionnaire.allowedActions.includes('COMPLETE')}
                onClick={preComplete}>
                <FormattedMessage id='complete' />
              </Button>
            )}
          </Grid>
          <Grid item >
            {questionnaire.allowedActions.includes('NEXT') && (
              <Button variant='contained' onClick={() => { window.scrollTo(0, 0); fillActions.next(); }} endIcon={<ChevronRightIcon />}>
                <FormattedMessage id='page.next' />
              </Button>
            )}
          </Grid>
        </Grid>
      )}
      <Dialog open={completeConfirmationOpen}>
        <DialogTitle><FormattedMessage id='complete.confirmation.title' /></DialogTitle>
        <DialogContent>
          <Typography variant='body1'>
            <FormattedMessage id='complete.confirmation.message' />
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCompleteConfirmationOpen(false)} color='secondary'><FormattedMessage id='complete.confirmation.cancel' /></Button>
          <Button onClick={() => fillActions.complete()} color='primary' autoFocus><FormattedMessage id='complete.confirmation.confirm' /></Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
