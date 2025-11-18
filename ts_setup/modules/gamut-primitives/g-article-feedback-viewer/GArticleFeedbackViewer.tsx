import React from 'react';
import {
  Box, Button, DialogActions, DialogContent, DialogTitle, Divider, Typography, IconButton,
  Stack, Tooltip, Popover,
  Alert
} from '@mui/material';

import { ThumbDownOffAlt as ThumbDownOffAltIcon } from '@mui/icons-material';
import { ThumbUpOffAlt as ThumbUpOffAltIcon } from '@mui/icons-material';
import { ThumbDown as ThumbDownIcon } from '@mui/icons-material';
import { ThumbUp as ThumbUpIcon } from '@mui/icons-material';
import { Info as InfoIcon } from '@mui/icons-material';

import { SiteApi, useSite } from '@dxs-ts/gamut-api';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';
import { useLocale } from '@dxs-ts/gamut-api';
import { useIam } from '@dxs-ts/gamut-api';
import { useUtilityClasses, GArticleFeedbackViewerRoot } from './useUtilityClasses';


export interface GArticleFeedbackViewerProps {
  onClose: () => void;
  feedbackId: SiteApi.FeedbackId;

}


export const GArticleFeedbackViewer: React.FC<GArticleFeedbackViewerProps> = (props) => {
  const intl = useIntl();
  const site = useSite();

  const { locale } = useLocale();
  const { authType } = useIam();
  const classes = useUtilityClasses();

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
    <GArticleFeedbackViewerRoot className={classes.root} open={true} maxWidth='lg' fullWidth onClose={props.onClose}>
      <DialogTitle className={classes.titleContainer}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Typography className={classes.title}>
            {intl.formatMessage({ id: 'gamut.feedback.feedbackViewerTitle' })}
          </Typography>

          <Typography sx={{ fontSize: '0.875rem', fontWeight: 'bold' }}>
            {intl.formatMessage({ id: 'gamut.feedback.updatedOnDate' })}
            {intl.formatMessage({ id: 'gamut.textSeparator', defaultMessage: ' ' })}
            {DateTime.fromJSDate(new Date(feedback.feedback.updatedOnDate))
              .setLocale(locale)
              .toLocaleString(DateTime.DATE_SHORT)}
          </Typography>
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
            <Box className={classes.loginReqPopoverMsgContainer}>
              <Alert icon={<InfoIcon />} severity='info' variant='standard'>
                <Typography>{intl.formatMessage({ id: 'gamut.feedback.vote.loginReq' })}</Typography>
              </Alert>
            </Box>
          </Popover>

          <Typography>{intl.formatMessage({ id: 'gamut.feedback.vote.title' })}</Typography>
          <div className={classes.thumbsContainer}>
            <IconButton color='primary' onClick={handleDownvote} disabled={feedbackDisabled}>
              <Tooltip title={intl.formatMessage({ id: 'gamut.feedback.vote.notHelpful' })}>
                {userFeedbackRating === 1 ? <ThumbDownIcon className={classes.iconSize} /> : <ThumbDownOffAltIcon className={classes.iconSize} />}
              </Tooltip>
            </IconButton>
            <Typography>{feedback.feedback.thumbsDownCount}</Typography>

            <Box marginRight={1} />

            <IconButton color='primary' onClick={handleUpvote} disabled={feedbackDisabled}>
              <Tooltip title={intl.formatMessage({ id: 'gamut.feedback.vote.yesHelpful' })}>
                {userFeedbackRating === 5 ? <ThumbUpIcon className={classes.iconSize} /> : <ThumbUpOffAltIcon className={classes.iconSize} />}
              </Tooltip>
            </IconButton>
            <Typography>{feedback.feedback.thumbsUpCount}</Typography>
          </div>
        </Stack >

      </DialogTitle>
      <DialogContent>
        <Typography className={classes.subTitle}>{feedback.feedback.customerTitle}</Typography>
        <Typography
          sx={{
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-word',
          }}
        >
          {feedback.feedback.content.question}
        </Typography>
        <Divider className={classes.contentDivider} />
        <div className={classes.replyContainer}>
          <Typography className={classes.answerSubTitle}>
            {intl.formatMessage({ id: 'gamut.feedback.feedbackViewerSubTitle' })}
          </Typography>
          <Typography
            sx={{
              whiteSpace: 'pre-wrap',
              wordBreak: 'break-word',
            }}
          >
            {feedback.feedback.replyText}
          </Typography>
        </div>
      </DialogContent>
      <DialogActions>
        <Button variant='contained' onClick={props.onClose}>{intl.formatMessage({ id: 'gamut.buttons.close' })}</Button>
      </DialogActions>
    </GArticleFeedbackViewerRoot>
  )
}