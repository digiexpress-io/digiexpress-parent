import React from 'react';
import { useThemeProps } from '@mui/system';
import { useUtilityClasses, GInboxRoot, MUI_NAME } from './useUtilityClasses';

import { GInboxItem, GInboxItemProps } from './GInboxItem';
import { GInboxFormReview, GInboxFormReviewProps } from '../g-inbox-form-review';
import { GInboxAttachments, GInboxAttachmentsProps } from '../g-inbox-attachments';

import { CommsApi, useComms } from '../api-comms';
import { IamApi, useIam } from '../api-iam';
import { useContracts } from '../api-contract';

import { IntlShape, useIntl } from 'react-intl';



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
  const intl = useIntl();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const { subjects } = useComms();
  const { getContract } = useContracts();
  const iam = useIam();


  const InboxItem: React.ElementType<GInboxItemProps> = props.slots?.item ?? GInboxItem;
  const Attachments: React.ElementType<GInboxAttachmentsProps> = props.slots?.attachment ?? GInboxAttachments;
  const FormReview: React.ElementType<GInboxFormReviewProps> = props.slots?.formReview ?? GInboxFormReview;

  const getSenderName = (subject: CommsApi.Subject, iam: IamApi.IamBackendContextType, intl: IntlShape): string => {
    switch (true) {
      case iam.user !== undefined && Boolean(iam.user.userId):
        return (iam.user.userId);
      case subject.lastExchange === undefined:
        return (intl.formatMessage({ id: 'gamut.inbox.noMessages' }));
      case subject.lastExchange?.userName === '' || subject.lastExchange?.userName === undefined:
        return (intl.formatMessage({ id: 'cust.inbox.message.sender-name.org-user' }));
      default:
        return subject.lastExchange.userName;
    }
  };

  return (
    <GInboxRoot className={classes.root}>
      {subjects.map((subject) => {
        const contractId = subject.contractId;
        const contract = getContract(contractId);

        return (<InboxItem
          id={subject.id}
          key={subject.id}
          onClick={props.slotProps.item.onClick!}
          senderName={getSenderName(subject, iam, intl)}
          sentAt={subject.lastExchange?.created ?? subject.created}
          title={subject.name}
          subTitle={subject.lastExchange?.commentText ?? ''}
          contractStatus={contract?.status ? intl.formatMessage({ id: `gamut.forms.status.${contract.status}` }) : 'status unknown'}
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
        </InboxItem>)
      })}
    </GInboxRoot>)
}
