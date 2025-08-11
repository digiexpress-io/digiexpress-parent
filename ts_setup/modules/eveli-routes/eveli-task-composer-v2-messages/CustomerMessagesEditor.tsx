import React from 'react';
import { Box, styled, Avatar, generateUtilityClass, Typography, darken, TextField } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import composeClasses from '@mui/utils/composeClasses';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { TaskApi } from '@dxs-ts/eveli-api';

export interface CustomerMessagesEditorProps {
  task: TaskApi.Task;
}

export const CustomerMessagesEditor: React.FC<CustomerMessagesEditorProps> = ({ task }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const [message, setMessage] = React.useState('');

  const allExternalMessages = task.comments?.filter(c => c.external)
    .sort((a, b) => DateTime.fromISO(b.created).toMillis() - DateTime.fromISO(a.created).toMillis())
    || [];


  const formatDate = (value: string): string => {
    try {
      return DateTime.fromISO(value).setLocale('fi').toLocaleString(DateTime.DATETIME_SHORT);
    } catch {
      return value;
    }
  };

  function handleOnChange(value: string) {
    setMessage(value);
  }

  return (

    <StyledCustomerMessagesEditor className={classes.container}>
      <Box className={classes.messagesContainer}>
        {allExternalMessages.length === 0 ?
          (
            <Typography textAlign='center'>{intl.formatMessage({ id: 'task.customerMessages.none', defaultMessage: 'No messages yet' })}</Typography>
          ) : (
          allExternalMessages.map((comment) => (
            <Box className={classes.messageRow} key={comment.id}>
              <Avatar className={comment.source === 'FRONTDESK' ? classes.frontdeskAvatar : classes.customerAvatar} />
              <Box className={comment.source === 'FRONTDESK' ? classes.frontdeskMessageBody : classes.customerMessageBody}>
                <Typography className={classes.senderInfo}>
                  {comment.userName}{intl.formatMessage({ id: 'user.message.wroteOn', defaultMessage: ' wrote on ' })}{formatDate(comment.created)}
                </Typography>
                <Typography style={{ overflow: 'hidden', whiteSpace: 'normal' }}>
                  {comment.commentText}
                </Typography>
              </Box>
            </Box>
          ))
        )}
      </Box>

      <Box className={classes.inputBox}>
        <Box className={classes.inputBoxTitle}>
          <EditIcon color='primary' />
          <Typography className={classes.messageBoxLabel}>
            {intl.formatMessage({ id: 'task.customerMessages.newMessageTitle', defaultMessage: 'Write a new message' })}
          </Typography>
        </Box>
        <StyledTextField value={message} fullWidth multiline rows={3}
          onChange={(e) => handleOnChange(e.target.value)}
          placeholder={intl.formatMessage({ id: 'task.customerMessages.newMessagePlaceholder', defaultMessage: 'My message to customer...' })} />
      </Box>
    </StyledCustomerMessagesEditor>

  );
};

const MUI_NAME = 'CustomerMessagesEditor';
const StyledCustomerMessagesEditor = styled('div', {
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
  height: '100%',
  overflow: 'hidden',

  '& .CustomerMessagesEditor-messagesContainer': {
    flexGrow: 1,
    overflowY: 'auto',
    padding: theme.spacing(4),
    margin: theme.spacing(2),
    border: `1px solid ${theme.palette.divider}`,
    borderRadius: theme.spacing(0.5),
    boxShadow: `0 4px 12px rgba(0, 0, 0, 0.05)`,
  },
  '& .CustomerMessagesEditor-messageBoxLabel': {
    ...theme.typography.body2,
    fontWeight: 'bold',
  },
  '& .CustomerMessagesEditor-messageRow': {
    display: 'flex',
    alignItems: 'center',
    marginBottom: theme.spacing(2),
  },
  '& .CustomerMessagesEditor-inputBoxTitle': {
    display: 'flex',
    alignItems: 'center',
    borderBottom: 'none',
    marginBottom: theme.spacing(2),

  },

  '& .CustomerMessagesEditor-inputBox': {
    position: 'sticky',
    width: '100%',
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2),
    marginTop: theme.spacing(1)
  },

  '& .CustomerMessagesEditor-senderInfo': {
    fontWeight: 'bold !important', //TODO figure out a better way with cardThemeConfig
  },

  '& .CustomerMessagesEditor-frontdeskAvatar': {
    border: `1px solid ${darken('#caf0f8', 0.1)}`,
    backgroundColor: '#caf0f8',
    marginRight: theme.spacing(1),
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
  },
  '& .CustomerMessagesEditor-customerAvatar': {
    border: `1px solid ${darken('#ecf39e', 0.1)}`,
    backgroundColor: '#ecf39e',
    marginRight: theme.spacing(1),
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
  },
  '& .CustomerMessagesEditor-frontdeskMessageBody': {
    flexGrow: 1,
    backgroundColor: '#caf0f8',
    borderRadius: '20px',
    padding: theme.spacing(1),
    border: `1px solid ${darken('#caf0f8', 0.1)}`,
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)',
  },
  '& .CustomerMessagesEditor-customerMessageBody': {
    flexGrow: 1,
    backgroundColor: '#ecf39e',
    borderRadius: '20px',
    padding: theme.spacing(1),
    border: `1px solid ${darken('#ecf39e', 0.1)}`,
    boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)'
  },

  '& .MuiSvgIcon-root': {
    fontSize: '20pt',
    marginRight: theme.spacing(1)
  },
}));

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  marginTop: 0,
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
}));

export const useUtilityClasses = () => {
  const slots = {
    container: ['container'],
    messageRow: ['messageRow'],
    messageBoxLabel: ['messageBoxLabel'],
    messagesContainer: ['messagesContainer'],
    senderInfo: ['senderInfo'],
    inputBox: ['inputBox'],
    frontdeskAvatar: ['frontdeskAvatar'],
    customerAvatar: ['customerAvatar'],
    frontdeskMessageBody: ['frontdeskMessageBody'],
    customerMessageBody: ['customerMessageBody'],
    inputBoxTitle: ['inputBoxTitle']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};
