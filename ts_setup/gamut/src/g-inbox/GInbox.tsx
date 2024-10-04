import React from 'react';
import { useThemeProps } from '@mui/system';
import { useUtilityClasses, GInboxRoot, MUI_NAME } from './useUtilityClasses';

import { GInboxItem, GInboxItemProps } from './GInboxItem';
import { GInboxFormReview, GInboxFormReviewProps } from '../g-inbox-form-review';
import { GInboxAttachments, GInboxAttachmentsProps } from '../g-inbox-attachments';

import { useComms } from '../api-comms';
import { useIam } from '../api-iam';


export interface GInboxProps {
  children?: React.ReactNode;

  slots?: {
    item?: React.ElementType<GInboxItemProps>;
    attachment?: React.ElementType<GInboxAttachmentsProps>;
    formReview?: React.ElementType<GInboxFormReviewProps>;
  }

  slotProps: {
    item: Partial<GInboxItemProps>;
    attachment: Partial<GInboxAttachmentsProps>;
    formReview: Partial<GInboxFormReviewProps>;
  }
}


export const GInbox: React.FC<GInboxProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const { subjects } = useComms();
  const iam = useIam();


  const InboxItem: React.ElementType<GInboxItemProps> = props.slots?.item ?? GInboxItem;
  const Attachments: React.ElementType<GInboxAttachmentsProps> = props.slots?.attachment ?? GInboxAttachments;
  const FormReview: React.ElementType<GInboxFormReviewProps> = props.slots?.formReview ?? GInboxFormReview;

  return (
    <GInboxRoot className={classes.root}>
      {subjects.map((subject) => (
        <InboxItem
          id={subject.id}
          key={subject.id}
          onClick={props.slotProps.item.onClick!}
          senderName={subject.lastExchange?.userName ?? iam.user?.userId ?? ''}
          sentAt={subject.lastExchange?.created ?? subject.created}
          title={subject.name}
          subTitle={subject.lastExchange?.commentText ?? ''}
        >
          <FormReview
            key={subject.id}
            name={subject.name}
            subjectId={subject.id}
            onClick={props.slotProps.formReview.onClick!}
          />

          {subject.documents.map((doc) => (
            <Attachments
              key={doc.id}
              subjectId={subject.id}
              attachmentId={doc.id}
              name={doc.name}
              onClick={props.slotProps.attachment.onClick!}
            />
          ))}
        </InboxItem>
      ))}
    </GInboxRoot>)
}
