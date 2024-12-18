import React from 'react';
import { Avatar, Box, Breadcrumbs, Container, Divider, Drawer, Link, Typography, useTheme } from '@mui/material';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import HomeIcon from '@mui/icons-material/Home';

import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import {
  CommsApi, ContractApi, GAppBar, GFooter,
  GInboxMessages, GLayout, GShell, GShellClassName, GUserOverviewMenu, GUserOverviewMenuView,
  useComms, useContracts, useOffers, useSite
} from '../';


export interface RouterInboxSubjectProps {
  locale: string;
  viewId: GUserOverviewMenuView;
  subjectId: string;
}

export const RouterInboxSubject: React.FC<RouterInboxSubjectProps> = ({ locale, subjectId, viewId }) => {

  const nav = useNavigate();
  const ownerState = useOwnerState(subjectId);
  const slots = React.useMemo(() => (
    ownerState.isPending ?
      {} :
      {
        breadcrumbs: () => <Bread ownerState={ownerState} />,
        topTitle: () => <Top ownerState={ownerState} />,
        left: () => <Left ownerState={ownerState} />
      }
  ), [ownerState]);

  function handleLocale(locale: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { locale },
      to: '/secured/$locale/views/$viewId',
    })
  }

  return (
    <GShell>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => ownerState.onNav('user-overview')} viewId={viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={ownerState.onNav} defaultView='inbox' />
      </Drawer>
      <main role='main'>
        <Container><GLayout variant='secured-1-row-1-column' slots={slots} /></Container>
      </main>
      <footer role='footer'><GFooter /></footer>
    </GShell >
  );
}


type OwnerState = (OwnerStateLoaded | OwnerStatePending)
type OwnerStateTemplate = {
  onNav: (viewId: GUserOverviewMenuView | undefined) => void,
  subjectId: string,
  isPending: boolean
}
type OwnerStateLoaded = OwnerStateTemplate & { isPending: false, subject: CommsApi.Subject, contract: ContractApi.Contract, offerName: string }
type OwnerStatePending = OwnerStateTemplate & { isPending: true, subject: undefined, contract: undefined, offerName: undefined }

function useOwnerState(subjectId: string): OwnerState {
  const { getSubject } = useComms();
  const { site } = useSite();
  const { getContract } = useContracts();
  const { getLocalisedOfferName } = useOffers();
  const nav = useNavigate();

  const subject = getSubject(subjectId);
  const contract = subject ? getContract(subject.contractId) : undefined;
  const offerName = site && contract ? getLocalisedOfferName(site, contract.offer.name) : undefined;
  const isPending = !site || !contract || !subject;

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



const Bread: React.FC<{ ownerState: OwnerStateLoaded }> = ({ ownerState }) => {
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

const Top: React.FC<{ ownerState: OwnerStateLoaded }> = ({ ownerState }) => {
  const theme = useTheme();
  const { offerName } = ownerState;
  return (
    <Box display='flex' flexDirection='row' alignItems='center'>
      <Avatar
        sx={{
          height: '50px',
          width: '50px',
          alignContent: 'center',
          mr: 1,
          backgroundColor: theme.palette.primary.main,
        }}
      >
        <MailOutlineIcon fontSize='large' />
      </Avatar>
      <Typography variant='h1'>{offerName}</Typography>
    </Box>);
}


const Left: React.FC<{ ownerState: OwnerStateLoaded }> = ({ ownerState }) => {
  function handleAttachmentClick(subjectId: string, attachmentId: string) { }

  const { subjectId } = ownerState;
  return (<>
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

