import React from 'react';
import { Avatar, Box, Breadcrumbs, Divider, generateUtilityClass, Link, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import HomeIcon from '@mui/icons-material/Home';

import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import { GUserOverviewMenuView } from '../g-user-overview-menu';
import { GInboxMessages } from '../g-inbox-messages';
import { CommsApi, useComms } from '../api-comms';
import { ContractApi, useContracts } from '../api-contract';
import { useSite } from '../api-site';
import { useOffers } from '../api-offer';


type OwnerState = (OwnerStateLoaded | OwnerStatePending)
type OwnerStateTemplate = {
  onNav: (viewId: GUserOverviewMenuView | undefined) => void,
  subjectId: string,
  isPending: boolean
}
type OwnerStateLoaded = OwnerStateTemplate & { isPending: false, subject: CommsApi.Subject, contract: ContractApi.Contract, offerName: string }
type OwnerStatePending = OwnerStateTemplate & { isPending: true, subject: undefined, contract: undefined, offerName: undefined }

export function useOwnerState(subjectId: string): OwnerState {
  const { getSubject } = useComms();
  const { site } = useSite();
  const { getContract } = useContracts();
  const { getLocalisedOfferName } = useOffers();
  const nav = useNavigate();

  const subject = getSubject(subjectId);
  const contract = subject ? getContract(subject.contractId) : undefined;
  const isPending = !site || !contract || !subject;
  const offerName = site && contract ? getLocalisedOfferName(site, contract.offer.name) : undefined;



  const ownerState: OwnerState = React.useMemo(() => {
    function onNav(viewId: GUserOverviewMenuView | undefined) {
      if (!viewId) { // i.e. --> login/logout buttons
        return;
      }
      nav({
        from: '/secured/$locale/views/$viewId',
        params: { viewId },
        to: '/secured/$locale/views/$viewId',
      })
    }

    if (isPending) {
      return {
        subjectId, onNav, isPending,
        subject: undefined,
        contract: undefined,
        offerName: undefined
      };
    }
    return {
      subjectId, onNav, isPending,
      subject: subject!,
      contract: contract!,
      offerName: offerName!
    };
  }, [isPending, subject, contract, offerName]);

  return ownerState;
}


export const MUI_NAME = 'GRouterInboxSubject';

export interface GRouterInboxSubjectClasses {
  root: string,
  title: string,
  topTitle: string,
  topTitleIcon: string,
  topTitleLayout: string
}
export type GRouterInboxSubjectClassKey = keyof GRouterInboxSubjectClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    topTitle: ['topTitle'],
    topTitleIcon: ['topTitleIcon'],
    topTitleLayout: ['topTitleLayout']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterInboxSubjectRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.title,
      styles.topTitle,
      styles.topTitleIcon,
      styles.topTitleLayout
    ];
  },
})(({ theme }) => {
  return {
    '.GRouterInboxSubject-topTitleLayout': {
      display: 'flex',
      flexDirection: 'row',
      alignItems: 'center'
    },
    '.GRouterInboxSubject-topTitle': {
      height: '50px',
      width: '50px',
      alignContent: 'center',
      marginRight: theme.spacing(1),
      backgroundColor: theme.palette.primary.main,
    },
    '.GRouterInboxSubject-topTitleIcon': {
      fontSize: '20pt'
    },
    '.GRouterInboxSubject-title': {
      ...theme.typography.h1
    }
  };
});


export const Bread: React.FC<{ ownerState: OwnerStateLoaded }> = ({ ownerState }) => {
  const intl = useIntl();
  const { subject, onNav } = ownerState;

  return (
    <Breadcrumbs>
      <Link onClick={() => onNav('user-overview')}>
        <HomeIcon />
        {intl.formatMessage({ id: 'gamut.userOverview.home' })}
      </Link>
      <Link onClick={() => onNav('inbox')}>{intl.formatMessage({ id: 'gamut.inbox.title' })}</Link>
      <Typography>{intl.formatMessage({ id: 'gamut.subjectMessage.title' }, { subject: subject.name })}</Typography>
    </Breadcrumbs>
  )
}

export const Top: React.FC<{ ownerState: OwnerStateLoaded }> = ({ ownerState }) => {
  const { offerName } = ownerState;
  const classes = useUtilityClasses();

  return (
    <Box className={classes.topTitleLayout}>
      <Avatar className={classes.topTitle}>
        <MailOutlineIcon className={classes.topTitleIcon} />
      </Avatar>
      <Typography className={classes.title}>{offerName}</Typography>
    </Box>);
}


export const Left: React.FC<{ ownerState: OwnerStateLoaded }> = ({ ownerState }) => {
  function handleAttachmentClick(subjectId: string, attachmentId: string) { }
  const { subjectId } = ownerState;

  return (
    <>
      <Divider />
      <GInboxMessages subjectId={subjectId}
        slotProps={{
          formReview: {},
          attachments: { onClick: handleAttachmentClick },
          message: {},
          newMessage: {}
        }}
      />
    </>)
}