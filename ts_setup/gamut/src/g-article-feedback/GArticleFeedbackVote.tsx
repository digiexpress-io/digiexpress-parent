import React from 'react';
import { Box, IconButton, Stack, Tooltip, Typography } from '@mui/material';
import ThumbDownIcon from '@mui/icons-material/ThumbDown';
import ThumbDownOffAltIcon from '@mui/icons-material/ThumbDownOffAlt';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import ThumbUpOffAltIcon from '@mui/icons-material/ThumbUpOffAlt';

import { SiteApi } from '../api-site';
import { useIntl } from 'react-intl';


interface GArticleFeedbackVoteProps {
  feedback: SiteApi.Feedback,
  className: string,
  readOnly: boolean,
}

export const GArticleFeedbackVote: React.FC<GArticleFeedbackVoteProps> = (props) => {
  const intl = useIntl();

  if (props.readOnly) {
    return (
      <div className={props.className}>
        <ThumbDownIcon fontSize='small' /><Typography>{props.feedback.thumbsDownCount}</Typography>
        <ThumbUpIcon fontSize='small' /><Typography>{props.feedback.thumbsUpCount}</Typography>
      </div>
    );
  }


  return (
    <Stack spacing={1}>
      <Typography>{intl.formatMessage({ id: 'gamut.feedback.vote.title' })}</Typography>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'end' }}>
        <IconButton color='primary'>
          <Tooltip title={intl.formatMessage({ id: 'gamut.feedback.vote.notHelpful' })}>
            <ThumbDownOffAltIcon fontSize='large' />
          </Tooltip>
        </IconButton>
        <Typography>{props.feedback.thumbsDownCount}</Typography>

        <Box marginRight={1} />

        <IconButton color='primary'>
          <Tooltip title={intl.formatMessage({ id: 'gamut.feedback.vote.yesHelpful' })}>
            <ThumbUpOffAltIcon fontSize='large' />
          </Tooltip>
        </IconButton>
        <Typography>{props.feedback.thumbsUpCount}</Typography>
      </div>
    </Stack >
  );
}



