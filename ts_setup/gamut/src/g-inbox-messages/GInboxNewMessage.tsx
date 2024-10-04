import React from 'react';
import { Button, TextField, Typography, useThemeProps } from '@mui/material';
import ReplyIcon from '@mui/icons-material/Reply';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import AttachFileIcon from '@mui/icons-material/AttachFile';

import { useIntl } from 'react-intl';
import { GInboxNewMessageRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';

export interface GInboxNewMessageProps {
  senderName?: string;
  subjectName: string;
  minRows?: number | undefined;
}

export const GInboxNewMessage: React.FC<GInboxNewMessageProps> = (initProps) => {
  const intl = useIntl();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const ownerState = {
    ...props,
    minRows: 5
  };

  const classes = useUtilityClasses();

  return (
    <GInboxNewMessageRoot className={classes.newMsgRoot}>
      <div className={classes.newMsgTitle}>
        {props.senderName ?
          <Typography className={classes.newMsgSenderName}>
            {intl.formatMessage({ id: 'gamut.inbox.newMessage.replyingTo' })}
            {props.senderName}
          </Typography> :
          <Typography className={classes.newMsgSenderName}>
            {intl.formatMessage({ id: 'gamut.inbox.newMessage.sendNew' })}
          </Typography>}
        <Typography>{intl.formatMessage({ id: 'gamut.inbox.newMessage.replyingTo.subject' })}{props.subjectName}</Typography>
      </div>

      <TextField multiline minRows={ownerState.minRows} placeholder={intl.formatMessage({ id: 'gamut.inbox.newMessage.placeholder' })} />

      <div className={classes.newMsgButtons}>
        <Button startIcon={<ReplyIcon />} variant='contained'>
          {intl.formatMessage({ id: 'gamut.buttons.reply' })}
        </Button>
        <Button className={classes.newMsgAddButton} startIcon={<AttachFileIcon />} variant='outlined'>
          {intl.formatMessage({ id: 'gamut.buttons.attachment.add' })}
        </Button>
        <Button startIcon={<DeleteForeverIcon />} className={classes.newMsgCancelButton} variant='outlined'>
          <Typography>{intl.formatMessage({ id: 'gamut.buttons.cancel' })}</Typography>
        </Button>
      </div>
    </GInboxNewMessageRoot>
  )
}

