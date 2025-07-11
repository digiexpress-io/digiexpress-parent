import React from 'react';
import { Box, styled, Avatar, generateUtilityClass, Typography, darken } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { TaskCardStyleDefinition } from './cardThemeConfig';
import { TaskApi } from '@/api-task';
import { DateTime } from 'luxon';

export interface CustomerMessagesProps {
  style: TaskCardStyleDefinition;
  task: TaskApi.Task;
}

export const CustomerMessages: React.FC<CustomerMessagesProps> = ({ task, style }) => {
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

  console.log(allExternalMessages.length)


  return (

    <StyledCustomerMessages className={classes.container}>
      {externalMessages.length === 0 && (
        <Typography sx={{ ...style }}>
          No messages
        </Typography>
      )}

      {externalMessages
        .slice(0, 3)
        .map((comment) => (
          <Box className={classes.messageRow} key={comment.id}>
            <Avatar className={comment.source === 'FRONTDESK' ? classes.frontdeskAvatar : classes.customerAvatar} />
            <Box className={comment.source === 'FRONTDESK' ? classes.frontdeskMessageBody : classes.customerMessageBody}>
              <Typography className={classes.senderInfo} sx={{ ...style.bodyTypography }} >
                {comment.userName} wrote on {formatDate(comment.created)}
              </Typography>
              <Typography style={{ ...style.bodyTypography }} className={classes.commentText}>
                {comment.commentText}
              </Typography>
            </Box>
          </Box>
        ))}

      {allExternalMessages.length > 3 && (
        <Typography sx={{ ...style.bodyTypography }}>
          ...{allExternalMessages.length - 3} more...
        </Typography>
      )}

    </StyledCustomerMessages>
  );
};

const MUI_NAME = 'CustomerMessages';
const StyledCustomerMessages = styled('div', {
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

  '& .CustomerMessages-messageRow': {
    display: 'flex',
    alignItems: 'center',
  },
  '& .CustomerMessages-commentText': {
    overflow: 'hidden',
    display: '-webkit-box',
    WebkitLineClamp: 3,
    WebkitBoxOrient: 'vertical',
    wordBreak: 'break-word',
  },
  '& .CustomerMessages-senderInfo': {
    fontWeight: 'bold !important', //TODO figure out a better way with cardThemeConfig
  },
  '& .CustomerMessages-frontdeskAvatar': {
    border: `1px solid ${darken('#caf0f8', 0.1)}`,
    backgroundColor: '#caf0f8',
    marginRight: theme.spacing(1),
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
  },
  '& .CustomerMessages-customerAvatar': {
    border: `1px solid ${darken('#ecf39e', 0.1)}`,
    backgroundColor: '#ecf39e',
    marginRight: theme.spacing(1),
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
  },
  '& .CustomerMessages-frontdeskMessageBody': {
    flexGrow: 1,
    backgroundColor: '#caf0f8',
    borderRadius: '20px',
    padding: theme.spacing(1),
    border: `1px solid ${darken('#caf0f8', 0.1)}`,
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)',
  },
  '& .CustomerMessages-customerMessageBody': {
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
    commentText: ['commentText'],
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
