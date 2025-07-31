import React from 'react';
import { Box, styled, Avatar, generateUtilityClass, Typography, darken } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card';
import { TaskApi } from '@/api-task';

export interface CustomerMessagesReadOnlyProps {
  style: TaskCardStyleDefinition;
  task: TaskApi.Task;
}

export const CustomerMessagesReadOnly: React.FC<CustomerMessagesReadOnlyProps> = ({ task, style }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  const allExternalMessages = task.comments?.filter(c => c.external)
    .sort((a, b) => DateTime.fromISO(b.created).toMillis() - DateTime.fromISO(a.created).toMillis())
    || [];

  const externalMessages = allExternalMessages.slice(0, 3);

  const formatDate = (value: string): string => {
    try {
      return DateTime.fromISO(value).setLocale('fi').toLocaleString(DateTime.DATETIME_SHORT);
    } catch {
      return value;
    }
  };


  const truncateText = (text: string, maxLength: number): string => {
    if (text.length <= maxLength) return text;
    return text.slice(0, maxLength) + '…';
  };


  return (

    <StyledCustomerMessagesReadOnly className={classes.container}>
      {externalMessages.length === 0 && (
        <Typography sx={{ ...style }}>
          {intl.formatMessage({ id: 'task.customerMessages.none', defaultMessage: 'No messages yet' })}
        </Typography>
      )}

      {externalMessages
        .slice(0, 3)
        .map((comment) => (
          <Box className={classes.messageRow} key={comment.id}>
            <Avatar className={comment.source === 'FRONTDESK' ? classes.frontdeskAvatar : classes.customerAvatar} />
            <Box className={comment.source === 'FRONTDESK' ? classes.frontdeskMessageBody : classes.customerMessageBody}>
              <Typography className={classes.senderInfo} sx={{ ...style.bodyTypography }} >
                {comment.userName}{intl.formatMessage({ id: 'user.message.wroteOn', defaultMessage: ' wrote on ' })}{formatDate(comment.created)}
              </Typography>
              <Typography style={{ ...style.bodyTypography, overflow: 'hidden', whiteSpace: 'normal' }}>
                {truncateText(comment.commentText, 200)}
              </Typography>
            </Box>
          </Box>
        ))}

      {allExternalMessages.length > 3 && (
        <Typography sx={{ ...style.bodyTypography }}>
          ...{allExternalMessages.length - 3} more...
        </Typography>
      )}

    </StyledCustomerMessagesReadOnly>
  );
};

const MUI_NAME = 'CustomerMessagesReadOnly';
const StyledCustomerMessagesReadOnly = styled('div', {
  name: MUI_NAME,
  slot: 'Message',
  overridesResolver: (_props, styles) => {
    return [
      styles.container
    ];
  },
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(2),

  '& .CustomerMessagesReadOnly-messageRow': {
    display: 'flex',
    alignItems: 'center',
  },

  '& .CustomerMessagesReadOnly-senderInfo': {
    fontWeight: 'bold !important', //TODO figure out a better way with cardThemeConfig
  },
  '& .CustomerMessagesReadOnly-frontdeskAvatar': {
    border: `1px solid ${darken('#caf0f8', 0.1)}`,
    backgroundColor: '#caf0f8',
    marginRight: theme.spacing(1),
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
  },
  '& .CustomerMessagesReadOnly-customerAvatar': {
    border: `1px solid ${darken('#ecf39e', 0.1)}`,
    backgroundColor: '#ecf39e',
    marginRight: theme.spacing(1),
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
  },
  '& .CustomerMessagesReadOnly-frontdeskMessageBody': {
    flexGrow: 1,
    backgroundColor: '#caf0f8',
    borderRadius: '20px',
    padding: theme.spacing(1),
    border: `1px solid ${darken('#caf0f8', 0.1)}`,
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)',
  },
  '& .CustomerMessagesReadOnly-customerMessageBody': {
    flexGrow: 1,
    backgroundColor: '#ecf39e',
    borderRadius: '20px',
    padding: theme.spacing(1),
    border: `1px solid ${darken('#ecf39e', 0.1)}`,
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
  },

  '& .MuiSvgIcon-root': {
    fontSize: '10pt',
    marginRight: theme.spacing(1),
    marginLeft: theme.spacing(1),
  },
}));

export const useUtilityClasses = () => {
  const slots = {
    container: ['container'],
    messageRow: ['messageRow'],
    senderInfo: ['senderInfo'],
    frontdeskAvatar: ['frontdeskAvatar'],
    customerAvatar: ['customerAvatar'],
    frontdeskMessageBody: ['frontdeskMessageBody'],
    customerMessageBody: ['customerMessageBody']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};
