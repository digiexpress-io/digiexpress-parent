import React from 'react';
import { Box, Typography, Stack } from '@mui/material';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import ThumbDownIcon from '@mui/icons-material/ThumbDown';
import { useIntl } from 'react-intl';



export interface ApprovalCountProps {
  approvalCount: number;
  disapprovalCount: number;
}


export const ApprovalCount: React.FC<ApprovalCountProps> = ({ approvalCount, disapprovalCount }) => {
  const intl = useIntl();

  return (
    <Box display='flex' alignItems='center' gap={1}>
      <Typography variant='caption' alignSelf='end' mr={1}>{intl.formatMessage({ id: 'feedback.customerApprovals' })}</Typography>

      <Stack spacing={1}>
        <ThumbDownIcon color='error' />
        <Typography alignSelf='center' variant='caption' fontWeight='bold'>
          {disapprovalCount}
        </Typography>
      </Stack>

      <Stack spacing={1}>
        <ThumbUpIcon color='success' />
        <Typography alignSelf='center' variant='caption' fontWeight='bold'>
          {approvalCount}
        </Typography>
      </Stack>
    </Box>
  )
}