import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';

import { SiteApi } from '../api-site';
import { GArticleFeedbackVote } from './GArticleFeedbackVote';
import { useIntl } from 'react-intl';
import { GMarkdown } from '../g-md';


interface GArticleFeedbackViewerProps {
  open: boolean;
  onClose: () => void;
  feedback: SiteApi.Feedback | undefined;
  className: string;
}


export const GArticleFeedbackViewer: React.FC<GArticleFeedbackViewerProps> = (props) => {
  const intl = useIntl();

  if (!props.feedback) {
    return (<>...Loading</>)
  }

  return (
    <Dialog open={props.open} onClose={props.onClose} className={props.className} maxWidth='lg' fullWidth>
      <DialogTitle>
        <Box display='flex'>
          <Typography>Customer feedback title</Typography>
          <Box flexGrow={1} />
          <GArticleFeedbackVote feedback={props.feedback} className={props.className} readOnly={false} />
        </Box>
      </DialogTitle>
      <DialogContent>
        <GMarkdown>{props.feedback.content}</GMarkdown>
        <Typography>Organization reply</Typography>
        <GMarkdown>{props.feedback.replyText}</GMarkdown>

      </DialogContent>
      <DialogActions>
        <Button variant='contained' onClick={props.onClose}>{intl.formatMessage({ id: 'gamut.buttons.close' })}</Button>
      </DialogActions>
    </Dialog>)
}