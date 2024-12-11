import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Typography } from '@mui/material';

import { SiteApi } from '../api-site';
import { GArticleFeedbackVote } from './GArticleFeedbackVote';
import { useIntl } from 'react-intl';
import { GMarkdown } from '../g-md';
import { DateTime } from 'luxon';
import { useLocale } from '../api-locale';


interface GArticleFeedbackViewerProps {
  open: boolean;
  onClose: () => void;
  feedback: SiteApi.Feedback | undefined;
  className: string;
}


export const GArticleFeedbackViewer: React.FC<GArticleFeedbackViewerProps> = (props) => {
  const intl = useIntl();
  const { locale } = useLocale();

  if (!props.feedback) {
    return (<></>)
  }

  console.log(props.feedback)

  return (
    <Dialog open={props.open} onClose={props.onClose} className={props.className} maxWidth='lg' fullWidth>
      <DialogTitle>
        <Box display='flex'>
          <div>
            <Typography variant='h3'>Customer feedback title</Typography>
            <Typography>
              {intl.formatMessage({ id: 'gamut.feedback.updatedOnDate' })}
              {intl.formatMessage({ id: 'gamut.textSeparator' })}
              {DateTime.fromJSDate(new Date(props.feedback.updatedOnDate))
                .setLocale(locale)
                .toLocaleString(DateTime.DATE_SHORT)}</Typography>
          </div>
          <Box flexGrow={1} />
          <GArticleFeedbackVote feedback={props.feedback} className={props.className} readOnly={false} />
        </Box>
      </DialogTitle>
      <DialogContent>
        <GMarkdown>{props.feedback.content}</GMarkdown>
        <Divider sx={{ my: 2 }} />
        <Typography fontWeight='bold' mb={2}>Organization reply</Typography>
        <GMarkdown>{props.feedback.replyText}</GMarkdown>
      </DialogContent>
      <DialogActions>
        <Button variant='contained' onClick={props.onClose}>{intl.formatMessage({ id: 'gamut.buttons.close' })}</Button>
      </DialogActions>
    </Dialog>)
}