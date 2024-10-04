import React from 'react';
import { Avatar, Box, Breadcrumbs, Container, Divider, Drawer, Link, Typography, useTheme } from '@mui/material';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import HomeIcon from '@mui/icons-material/Home';

import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';

import {
  GShell,
  GLayout,
  GFooter,
  GShellClassName,
  GUserOverviewMenuView,
  GAppBar,
  GUserOverviewMenu,
  GInbox,
} from '../';




export interface RouterInboxProps {
  locale: string;
  viewId: GUserOverviewMenuView;
}

export const RouterInbox: React.FC<RouterInboxProps> = ({ locale, viewId }) => {
  const intl = useIntl();
  const theme = useTheme();


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

  function handleSubjectClick(subjectId: string) {
    nav({
      from: '/secured/$locale/views/$viewId',
      params: { subjectId },
      to: '/secured/$locale/views/$viewId/$subjectId',
    })
  }

  function handleAttachmentClick(subjectId: string, attachmentId: string) {

  }

  function handleFormReviewClick(subjectId: string) {

  }

  return (
    <GShell>
      <GAppBar locale={locale} onLocale={handleLocale} onLogoClick={() => handleClick('user-overview')} viewId={viewId} />
      <Drawer variant='permanent' open={false} className={GShellClassName}>
        <GUserOverviewMenu onClick={handleClick} defaultView='inbox' />
      </Drawer>
      <main role='main'>
        <Container>
          <GLayout variant='secured-1-row-1-column'
            slots={{
              breadcrumbs: () => (
                <Breadcrumbs>
                  <Link onClick={() => handleClick('user-overview')}>
                    <HomeIcon />
                    {intl.formatMessage({ id: 'gamut.userOverview.home' })}
                  </Link>
                  <Typography>{intl.formatMessage({ id: 'gamut.inbox.title' })}</Typography>
                </Breadcrumbs>
              ),
              topTitle: () => (<>
                <Box display='flex' flexDirection='row' alignItems='center'>
                  <Avatar sx={{ height: '50px', width: '50px', alignContent: 'center', mr: 1, backgroundColor: theme.palette.primary.main }}>
                    <MailOutlineIcon fontSize='large' />
                  </Avatar>
                  <Typography variant='h1'>{intl.formatMessage({ id: 'gamut.inbox.title' })}</Typography>
                </Box>
                <Typography variant='body1' my={theme.spacing(1)}>{intl.formatMessage({ id: 'gamut.inbox.subtitle' })}</Typography>
              </>
              ),
              left: () => (<>
                <Divider />
                <GInbox slotProps={{
                  attachment: { onClick: handleAttachmentClick },
                  formReview: { onClick: handleFormReviewClick },
                  item: { onClick: handleSubjectClick }
                }}
                />

              </>
              )
            }} />
        </Container>
      </main >
      <footer role='footer'>
        <GFooter />
      </footer>
    </GShell >
  );
}

