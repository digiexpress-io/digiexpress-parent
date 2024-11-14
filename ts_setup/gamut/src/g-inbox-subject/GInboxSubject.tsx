import React from 'react';
import { Box, Divider, Typography, useThemeProps } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { GTooltip } from '../g-tooltip';
import { GInboxSubjectRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';

import { GInboxAttachments, GInboxAttachmentsProps } from '../g-inbox-attachments';
import { GInboxFormReview, GInboxFormReviewProps } from '../g-inbox-form-review';

import { GInboxSubjectNewMessageProps, GInboxSubjectNewMessage } from './GInboxSubjectNewMessage';
import { GInboxSubjectMessage, GInboxSubjectMessageProps } from './GInboxSubjectMessage';
import { useComms } from '../api-comms';



export interface GInboxSubjectProps {
  subjectId: string;
  slots?: {
    formReview?: React.ElementType<GInboxFormReviewProps>
    messages?: React.ElementType<GInboxSubjectMessageProps>;
    newMessage?: React.ElementType<GInboxSubjectNewMessageProps>;
    attachments?: React.ElementType<GInboxAttachmentsProps>;
  }

  slotProps: {
    formReview: Partial<GInboxFormReviewProps>
    messages: Partial<GInboxSubjectMessageProps>;
    newMessage: Partial<GInboxSubjectNewMessageProps>;
    attachments: Partial<GInboxAttachmentsProps>;
  }
}


export const GInboxSubject: React.FC<GInboxSubjectProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const { getSubject } = useComms();

  const subject = getSubject(props.subjectId);

  if (!subject) {
    return <></>;
  }

  const FormReview: React.ElementType<GInboxFormReviewProps> = props.slots?.formReview ?? GInboxFormReview;
  const Messages: React.ElementType<GInboxSubjectMessageProps> = props.slots?.messages ?? GInboxSubjectMessage;
  const NewMessage: React.ElementType<GInboxSubjectNewMessageProps> = props.slots?.newMessage ?? GInboxSubjectNewMessage;
  const Attachments: React.ElementType<GInboxAttachmentsProps> = props.slots?.attachments ?? GInboxAttachments;

  return (
    <GInboxSubjectRoot className={classes.root}>
      <Box className={classes.title}>
        <GTooltip title={'This is a tooltip with a test sentence. It is very helpful indeed!'}>
          <Typography><FormattedMessage id='gamut.inbox.subjectAttachment.title' /></Typography>
        </GTooltip>
      </Box>
      <>
        <div className={classes.header}>
          <FormReview name={subject.name} onClick={props.slotProps.formReview.onClick!} subjectId={subject.id} />

          {subject?.documents.map((doc) => (
            <div className={classes.attachments}>
              <Attachments name={doc.name}
                subjectId={subject.id}
                attachmentId={doc.id}
                onClick={() => { }}
                key={subject.id}
              />
            </div>
          ))}
        </div>

        <Divider />

        <div className={classes.messages}>

          {subject?.exchange.map((exchange) => (
            <Messages commentText={exchange.commentText}
              created={exchange.created}
              isMyMessage={exchange.isMyMessage}
              senderName={exchange.userName}
              key={exchange.id}
            />
          ))}
        </div>

        <div className={classes.newMessage}>
          <NewMessage subjectName={subject.name} />
        </div>
      </>

    </GInboxSubjectRoot>
  )
}

