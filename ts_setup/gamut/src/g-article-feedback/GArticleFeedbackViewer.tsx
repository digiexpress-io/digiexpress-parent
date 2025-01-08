import React from 'react';
import {
  Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Typography, IconButton,
  Stack, Tooltip, Popover, useTheme, alpha,
  Alert
} from '@mui/material';

import ThumbDownOffAltIcon from '@mui/icons-material/ThumbDownOffAlt';
import ThumbUpOffAltIcon from '@mui/icons-material/ThumbUpOffAlt';
import ThumbDownIcon from '@mui/icons-material/ThumbDown';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import InfoIcon from '@mui/icons-material/Info';

import { SiteApi, useSite } from '../api-site';
import { useIntl } from 'react-intl';
import { GMarkdown } from '../g-md';
import { DateTime } from 'luxon';
import { useLocale } from '../api-locale';
import { useIam } from '../api-iam';


interface GArticleFeedbackViewerProps {
  onClose: () => void;
  feedbackId: SiteApi.FeedbackId;
  className: string;
}


export const GArticleFeedbackViewer: React.FC<GArticleFeedbackViewerProps> = (props) => {
  const intl = useIntl();
  const site = useSite();

  const theme = useTheme();
  const { locale } = useLocale();
  const { authType } = useIam();

  const feedback = site.feedback.find(f => f.feedback.id === props.feedbackId);
  const userFeedbackRating: 1 | 5 | undefined = feedback?.rating?.rating as any;
  const feedbackDisabled = authType === 'ANON';

  if (!feedback) {
    return <></>
  }


  function handleUpvote() {
    site.voteOnReply({ rating: userFeedbackRating === 5 ? undefined : 5, replyIdOrCategoryId: feedback!.feedback.id });
  }

  function handleDownvote() {
    site.voteOnReply({ rating: userFeedbackRating === 1 ? undefined : 1, replyIdOrCategoryId: feedback!.feedback.id });
  }

  const [anchorEl, setAnchorEl] = React.useState<HTMLElement | null>(null);

  const handlePopoverOpen = (event: React.MouseEvent<HTMLElement>) => {
    if (feedbackDisabled) {
      setAnchorEl(event.currentTarget);
    }
  };

  const handlePopoverClose = () => {
    setAnchorEl(null);
  };

  const open = Boolean(anchorEl);

  return (
    <Dialog open={true} onClose={props.onClose} className={props.className} maxWidth='lg' fullWidth>
      <DialogTitle>
        <Box display='flex'>
          <div>
            <Typography variant='h3'>{intl.formatMessage({ id: 'gamut.feedback.feedbackViewerTitle' })}</Typography>
            <Typography>
              {intl.formatMessage({ id: 'gamut.feedback.updatedOnDate' })}
              {intl.formatMessage({ id: 'gamut.textSeparator' })}
              {DateTime.fromJSDate(new Date(feedback.feedback.updatedOnDate))
                .setLocale(locale)
                .toLocaleString(DateTime.DATE_SHORT)}</Typography>
          </div>
          <Box flexGrow={1} />

          <Stack spacing={1} onMouseEnter={handlePopoverOpen} onMouseLeave={handlePopoverClose}>
            <Popover sx={{ pointerEvents: 'none' }} open={open} anchorEl={anchorEl} disableRestoreFocus
              anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'center',
              }}
              transformOrigin={{
                vertical: 'top',
                horizontal: 'center',
              }}
              onClose={handlePopoverClose}
            >
              <Box display='flex' alignItems='center'>
                <Alert icon={<InfoIcon />} severity='info' variant='outlined' sx={{ backgroundColor: alpha(theme.palette.info.light, 0.1) }}>
                  <Typography>{intl.formatMessage({ id: 'gamut.feedback.vote.loginReq' })}</Typography>
                </Alert>
              </Box>
            </Popover>

            <Typography>{intl.formatMessage({ id: 'gamut.feedback.vote.title' })}</Typography>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'end' }}>
              <IconButton color='primary' onClick={handleDownvote} disabled={feedbackDisabled}>
                <Tooltip title={intl.formatMessage({ id: 'gamut.feedback.vote.notHelpful' })}>
                  {userFeedbackRating === 1 ? <ThumbDownIcon fontSize='large' /> : <ThumbDownOffAltIcon fontSize='large' />}
                </Tooltip>
              </IconButton>
              <Typography>{feedback.feedback.thumbsDownCount}</Typography>

              <Box marginRight={1} />

              <IconButton color='primary' onClick={handleUpvote} disabled={feedbackDisabled}>
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
        <Typography fontWeight='bold' mb={2}>{intl.formatMessage({ id: 'gamut.feedback.feedbackViewerSubTitle' })}</Typography>
        <GMarkdown>{feedback.feedback.replyText}</GMarkdown>
      </DialogContent>
      <DialogActions>
        <Button variant='contained' onClick={props.onClose}>{intl.formatMessage({ id: 'gamut.buttons.close' })}</Button>
      </DialogActions>
    </Dialog>)
}