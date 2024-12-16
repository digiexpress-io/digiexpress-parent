import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Typography, IconButton, Stack, Tooltip } from '@mui/material';

import ThumbDownOffAltIcon from '@mui/icons-material/ThumbDownOffAlt';
import ThumbUpOffAltIcon from '@mui/icons-material/ThumbUpOffAlt';
import ThumbDownIcon from '@mui/icons-material/ThumbDown';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';

import { SiteApi, useSite } from '../api-site';
import { useIntl } from 'react-intl';
import { GMarkdown } from '../g-md';
import { DateTime } from 'luxon';
import { useLocale } from '../api-locale';


interface GArticleFeedbackViewerProps {
  onClose: () => void;
  feedbackId: SiteApi.FeedbackId;
  className: string;
}


export const GArticleFeedbackViewer: React.FC<GArticleFeedbackViewerProps> = (props) => {
  const intl = useIntl();
  const { locale } = useLocale();
  const site = useSite();
  const feedback = site.feedback.find(f => f.feedback.id === props.feedbackId);
  const userFeedbackRating: 1 | 5 | undefined = feedback?.rating?.rating as any;

  if (!feedback) {
    return <></>
  }


  function handleUpvote() {
    site.voteOnReply({ rating: userFeedbackRating === 5 ? undefined : 5, replyIdOrCategoryId: feedback!.feedback.id });
  }

  function handleDownvote() {
    site.voteOnReply({ rating: userFeedbackRating === 1 ? undefined : 1, replyIdOrCategoryId: feedback!.feedback.id });
  }

  return (
    <Dialog open={true} onClose={props.onClose} className={props.className} maxWidth='lg' fullWidth>
      <DialogTitle>
        <Box display='flex'>
          <div>
            <Typography variant='h3'>Customer feedback title</Typography>
            <Typography>
              {intl.formatMessage({ id: 'gamut.feedback.updatedOnDate' })}
              {intl.formatMessage({ id: 'gamut.textSeparator' })}
              {DateTime.fromJSDate(new Date(feedback.feedback.updatedOnDate))
                .setLocale(locale)
                .toLocaleString(DateTime.DATE_SHORT)}</Typography>
          </div>
          <Box flexGrow={1} />

          <Stack spacing={1}>
            <Typography>{intl.formatMessage({ id: 'gamut.feedback.vote.title' })}</Typography>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'end' }}>
              <IconButton color='primary' onClick={handleDownvote}>
                <Tooltip title={intl.formatMessage({ id: 'gamut.feedback.vote.notHelpful' })}>
                  {userFeedbackRating === 1 ? <ThumbDownIcon fontSize='large' /> : <ThumbDownOffAltIcon fontSize='large' />}
                </Tooltip>
              </IconButton>
              <Typography>{feedback.feedback.thumbsDownCount}</Typography>

              <Box marginRight={1} />

              <IconButton color='primary' onClick={handleUpvote}>
                <Tooltip title={intl.formatMessage({ id: 'gamut.feedback.vote.yesHelpful' })}>
                  {userFeedbackRating === 5 ? <ThumbUpIcon fontSize='large' /> : <ThumbUpOffAltIcon fontSize='large' />}
                </Tooltip>
              </IconButton>
              <Typography>{feedback.feedback.thumbsUpCount}</Typography>
            </div>
          </Stack >

        </Box>
      </DialogTitle>
      <DialogContent>
        <GMarkdown>{feedback.feedback.content}</GMarkdown>
        <Divider sx={{ my: 2 }} />
        <Typography fontWeight='bold' mb={2}>Organization reply</Typography>
        <GMarkdown>{feedback.feedback.replyText}</GMarkdown>
      </DialogContent>
      <DialogActions>
        <Button variant='contained' onClick={props.onClose}>{intl.formatMessage({ id: 'gamut.buttons.close' })}</Button>
      </DialogActions>
    </Dialog>)
}