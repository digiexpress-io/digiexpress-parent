import React from 'react';
import { useUtilityClasses, GInboxRoot, MUI_NAME } from './useUtilityClasses';
import { useIntl } from 'react-intl';
import { Typography, useThemeProps, Grid2, Avatar } from '@mui/material';

import { GInboxItem, GInboxItemProps } from './GInboxItem';
import { GInboxFormReview, GInboxFormReviewProps } from '../g-inbox-form-review';
import { GInboxAttachmentsProps } from '../g-inbox-attachments';

import { CommsApi, OfferApi } from '@dxs-ts/gamut-api';

import { GFlex } from '../g-flex';
import { GInboxFormAssignedNotComplete } from '../g-inbox-form-assigned-not-complete';
import { useInboxItems, useSenderName } from './utils';



export interface GInboxProps {
  children?: React.ReactNode;
  onOpenOffer: (offer: OfferApi.Offer) => void;
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
  const inboxItems = useInboxItems();


  const InboxItem: React.ElementType<GInboxItemProps> = props.slots?.item ?? GInboxItem;
  const FormReview: React.ElementType<GInboxFormReviewProps> = props.slots?.formReview ?? GInboxFormReview;


  return (
    <GInboxRoot className={classes.root}>
      <GFlex variant='header'>
        <Grid2 container className={classes.headerRow}>

          <Grid2 size={{ xs: 12, sm: 12, md: 2, lg: 2 }}>
            <Typography fontWeight='bold'>
              {intl.formatMessage({ id: 'gamut.forms.taskRefId' })}
            </Typography>
          </Grid2>

          <Grid2 size={{ xs: 12, sm: 12, md: 4, lg: 4 }} className={classes.headerFormName}>
            <Typography fontWeight='bold'>
              {intl.formatMessage({ id: 'gamut.forms.formName' })}
            </Typography>
          </Grid2>

          <Grid2 size={{ xs: 12, sm: 12, md: 4, lg: 4 }} className={classes.headerAttachments}>
            <Typography fontWeight='bold'>
              {intl.formatMessage({ id: 'gamut.forms.attachments' })}
            </Typography>
          </Grid2>

          <Grid2 size={{ xs: 12, sm: 12, md: 2, lg: 2 }} className={classes.headerLastModified}>
            <Typography fontWeight='bold'>
              {intl.formatMessage({ id: 'gamut.forms.lastModified' })}
            </Typography>
          </Grid2>
        </Grid2>
      </GFlex>


      {inboxItems.map(({ subject, contract, offerName, contractStatus, subForms }) => {

          return (<InboxItem
            id={subject.id}
            taskRefId={contract.referenceId}
            key={subject.id}
            onClick={props.slotProps.item.onClick!}
            senderName={useSenderName(subject)}
            sentAt={subject.lastExchange?.created ?? subject.created}
            title={offerName}
            subTitle={subject.lastExchange?.commentText ?? ''}
            contractStatus={contractStatus}
          >

            {(() => {
             const documents = subject.documents.length;

              return (
                <>
                  <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
                    <Typography component='span' className={classes.files}>
                      {intl.formatMessage({ id: 'gamut.forms.attachments' })}
                    </Typography>
                  </GFlex>

                  {documents ? (
                    <Avatar className={classes.filesCount}>
                      <Typography>{documents}</Typography>
                    </Avatar>
                  ) : (
                    <Avatar className={classes.noValue}>
                      {intl.formatMessage({ id: 'gamut.noValueIndicator' })}
                    </Avatar>
                  )}
                </>
              );
            })()}

            {subForms.map(({ formId, formName, isOpen, offer }) => {
              if (isOpen && offer) {
                return (<GInboxFormAssignedNotComplete key={formId} onClick={() => props.onOpenOffer(offer)} formName={formName} />)
              }
              return (<FormReview key={formId} formName={offerName} formId={formId} />)
            })
            }
          </InboxItem>
          )
        })}
    </GInboxRoot>
  )
}
