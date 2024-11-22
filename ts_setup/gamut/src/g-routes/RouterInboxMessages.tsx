import React from 'react';
import { Avatar, Box, Breadcrumbs, Container, Divider, Drawer, Link, Typography, useTheme } from '@mui/material';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import HomeIcon from '@mui/icons-material/Home';

import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import { GAppBar, GFooter, GInboxMessages, GLayout, GShell, GShellClassName, GUserOverviewMenu, GUserOverviewMenuView, useComms, useContracts, useOffers, useSite } from '../';


export interface RouterInboxSubjectProps {
  locale: string;
  viewId: GUserOverviewMenuView;
  subjectId: string;
}

export const RouterInboxSubject: React.FC<RouterInboxSubjectProps> = ({ locale, subjectId, viewId }) => {

  const { getSubject } = useComms();
  const { site } = useSite();
  const { getContract } = useContracts();
  const { getLocalisedOfferName, getOffer } = useOffers();
  const theme = useTheme();
  const intl = useIntl();

  const nav = useNavigate();
  function handleLocale(locale: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { locale },

      to: '/secured/$locale/views/$viewId',
    })
  }
  function handleClick(viewId: GUserOverviewMenuView | undefined) {
    if (!viewId) { // i.e. --> login/logout buttons
      return;
    }
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId },
      to: '/secured/$locale/views/$viewId',
    })
  }

  function handleAttachmentClick(subjectId: string, attachmentId: string) { }
  function handleFormReviewClick(subjectId: string) { }

  const subject = getSubject(subjectId);

  if (!subject) {
    return <>...</>
  }

  const contract = getContract(subject.contractId);

  if (!site || !contract) {
    return <>...</>
  }

  const offerName = getLocalisedOfferName(site, contract?.offer.name);

  return (
    <GShell>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleClick} defaultView='inbox' />
      </Drawer>
      <main role='main'>
        <Container>
          <GLayout
            variant='secured-1-row-1-column'
            slots={{
              breadcrumbs: () => (
                <Breadcrumbs>
                  <Link onClick={() => handleClick('user-overview')}>
                    <HomeIcon />
                    {intl.formatMessage({ id: 'gamut.userOverview.home' })}
                  </Link>
                  <Link onClick={() => handleClick('inbox')}>{intl.formatMessage({ id: 'gamut.inbox.title' })}</Link>
                  <Typography>{intl.formatMessage({ id: 'gamut.subjectMessage.title' }, { subject: subject?.name })}</Typography>
                </Breadcrumbs>
              ),
              topTitle: () => (
                <>
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

                    <React.Fragment key={subject.id}>
                      <Typography variant='h1'>{offerName}</Typography>
                    </React.Fragment>
                  </Box>
                </>
              ),
              left: () => (
                <>
                  <Divider />

                  <GInboxMessages subjectId={subject.id}
                    slotProps={{
                      formReview: { onClick: handleFormReviewClick },
                      attachments: { onClick: handleAttachmentClick },
                      message: {},
                      newMessage: {}
                    }}
                  />
                </>
              ),
            }}
          />
        </Container>

      </main >
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell >
  );
}

