import React, { useContext, useEffect, useRef, useState } from 'react';
import { FeedbackContext } from '../context/FeedbackContext';
import { FormattedMessage, useIntl } from 'react-intl';
import { Dialog, DialogTitle, Button, DialogActions, DialogContent, TextField, FormControlLabel, Checkbox, Snackbar, useMediaQuery } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { green } from '@mui/material/colors';
import { useConfig } from '../context/ConfigContext';
import { useUserInfo } from '../context/UserContext';

const feedbackUrl = 'https://feedback.resys.io/api/feedback';

enum RequestState {
  NotSent, Loading, Success, Error,
}


interface Props { };
export const Feedback: React.FC<Props> = () => {
  const intl = useIntl();
  const userInfo = useUserInfo();
  const descriptionRef = useRef<HTMLTextAreaElement>(null);
  const [description, setDescription] = useState('');
  const [requestState, setRequestState] = useState(RequestState.NotSent);
  const [sendFeedback, setSendFeedback] = useState(false);
  const [name, setName] = useState(userInfo.user.name || '');
  const context = useContext(FeedbackContext);
  const theme = useTheme();
  const fullScreen = useMediaQuery(theme.breakpoints.down('sm'));
  const config = useConfig();

  useEffect(() => {
    if(!descriptionRef.current || !context.isOpen) return;
    descriptionRef.current.focus();
  }, [context.isOpen]);

  useEffect(() => {
    if(context.isOpen) {
      // pre-import feedback library after user opens modal
      import('@resys/feedback');
    }
  }, [context.isOpen]);

  //if (!feedbackKey || !feedbackUrl) return null;
  if (!config.feedbackKey) return null;

  function close() {
    setSendFeedback(false);
    setDescription('');
    setName('');
    setRequestState(RequestState.NotSent);
    context.close();
  }

  return (
    <>
      {requestState === RequestState.Success && (
        <Snackbar open={true} anchorOrigin={{vertical: 'top', horizontal: 'center'}} message={intl.formatMessage({id: 'feedback.thanks'})} 
        sx={{backgroundColor: green[600]}}/>
      )}
        <Dialog open={context.isOpen && requestState !== RequestState.Success} onClose={close} fullScreen={fullScreen}>
          <DialogTitle><FormattedMessage id='feedback.title'/></DialogTitle>
          <DialogContent dividers>
          <TextField
              fullWidth={true}
              label={intl.formatMessage({id: 'feedback.description'})}
              value={description}
              onChange={e => setDescription(e.target.value)}
              margin='normal'
              multiline
              rows={5}
              maxRows={5}
              inputRef={descriptionRef}
              variant="outlined"
            />
            <TextField
              fullWidth={true}
              label={intl.formatMessage({id: 'feedback.name'})}
              value={name}
              onChange={e => setName(e.target.value)}
              variant="outlined"
            />
            <FormControlLabel
              label= {intl.formatMessage({id: 'feedback.sendScreenshot'})}
              control={
                <Checkbox
                  checked={sendFeedback || false}
                  onChange={() => setSendFeedback(!sendFeedback)}
                  />
              }
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={close}><FormattedMessage id='feedback.close' /></Button>
            <Button onClick={async () => {
              setRequestState(RequestState.Loading);
              const FeedbackAPI = (await import('@resys/feedback')).default;
              try {
                const feedback = new FeedbackAPI(config.feedbackKey || '', feedbackUrl);
                if(sendFeedback) {
                  const root = document.getElementById('root');
                  if(root) {
                    await feedback.screenshot(root);
                  }
                }
                feedback.addMetadata('Name', name);
                feedback.addMetadata('URL', window.location.href);
                feedback.addMetadata('Browser', navigator.userAgent);
                if(context.beforeSend) {
                  context.beforeSend(feedback);
                }
                await feedback.send(description);
                setRequestState(RequestState.Success);
                setTimeout(() => {
                  setRequestState(RequestState.NotSent);
                  close();
                }, 2500);
              } catch (e) {
                setRequestState(RequestState.Error);
              }
            }}><FormattedMessage id='feedback.send' /></Button>
          </DialogActions>
        </Dialog>
    </>
  );
}