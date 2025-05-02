import React from 'react';
import { Typography, Button, List, ListItem, ListItemText, Alert } from '@mui/material';
import { useIntl } from 'react-intl';
import { useCanGoBack, useNavigate, useRouter } from '@tanstack/react-router';


import { GLayout } from '../g-layout';
import { useIam } from '../api-iam';
import { GUserOverviewMenuView } from '../g-user-overview-menu';


import { 
  useUtilityClasses,
  GFormUnavailableTitleSlot,
  GFormUnavailableButtonsSlot,
  GFormUnavailableRoot
} from './useUtilityClasses';


const CancelButton: React.FC<{}> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const iam = useIam();
  const nav = useNavigate();
  const router = useRouter();
  const canGoBack = useCanGoBack()


  function handleCancel() {
    if(canGoBack){
      router.history.back();

    } else if (iam.authType === 'ANON') {
      nav({
        from: '/public/$locale',
        to: '/public/$locale',
      })
    }
    else {
      const viewId: GUserOverviewMenuView = 'user-overview';
      nav({
        params: { viewId, locale: intl.locale },
        to: '/secured/$locale/views/$viewId',
      })
    }
  }

  return (
    <GFormUnavailableButtonsSlot className={classes.root}>
      <Button variant='outlined' onClick={handleCancel}>{intl.formatMessage({ id: 'gamut.forms.filling.cancel.button' })}</Button>
    </GFormUnavailableButtonsSlot>)
}

const ErrorTitle: React.FC<{}> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  return (<GFormUnavailableTitleSlot className={classes.title}>
      <Typography className={classes.pageTitle}>{intl.formatMessage({ id: 'gamut.forms.filling.error', defaultMessage: 'Activity could not be completed' })}</Typography>
      <List disablePadding dense>
        <ListItem>
          <ListItemText>
              <Alert severity='error' variant='filled'>
              <Typography>{intl.formatMessage({ id: 'gamut.forms.filling.unavailable', defaultMessage: "Form is unavailable at the this time" })}</Typography>
            </Alert>
          </ListItemText>
        </ListItem>
      </List>
    </GFormUnavailableTitleSlot>);
}


export interface GFormUnavailableProps {

}

export const GFormUnavailable: React.FC<GFormUnavailableProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <GLayout variant='fill-session-start-end'
      slots={{
        breadcrumbs: () => (<></>),
        topTitle: () => <></>,
        center: () => (
          <GFormUnavailableRoot className={classes.root}>

            <ErrorTitle />
            <CancelButton/>
          </GFormUnavailableRoot>)
      }}>
    </GLayout>
    
  )
}
