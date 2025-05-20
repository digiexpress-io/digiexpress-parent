import React from 'react';
import { Typography, List, ListItem, ListItemIcon, ListItemText, Alert, AlertTitle } from '@mui/material';

import SaveIcon from '@mui/icons-material/Save';
import { useIntl } from 'react-intl';

import { useIam } from '../api-iam';
import { useUtilityClasses } from './useUtilityClasses';
import { GRouterProductOwnerState } from './g-router-product-types'



export const GRouterProductContent: React.FC<GRouterProductOwnerState> = (props) => {
  const { topicLink } = props.ownerState;
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { user, authType } = useIam();
  const userName = (authType === 'USER' ? user?.firstName + " " + user?.lastName : user?.representedCompany?.name || user?.representedPerson?.name) ?? '-';
  const type = props.ownerState.status;


  return (
    <div className={classes.productTitle}>
      <Typography className={classes.productTitle}>{intl.formatMessage({ id: 'gamut.forms.filling.welcome' })}</Typography>
      <Typography className={classes.productSubTitle}>{intl.formatMessage({ id: 'gamut.forms.filling.start' })}{intl.formatMessage({ id: 'gamut.textSeparator' })}{topicLink?.name ?? "-"}</Typography>

      <List disablePadding dense>
        <ListItem>
          <ListItemIcon>
            <SaveIcon color='primary' />
          </ListItemIcon>
          <ListItemText>
            <Typography className={classes.productBodyText}>
              {intl.formatMessage({ id: 'gamut.forms.filling.start.info1' })}
            </Typography>
          </ListItemText>
        </ListItem>
        <ListItem>
          <ListItemText>

            {/* Alert with green box */}

            {/** NOT OK - NO BUTTON, WRONG USERNAME */}
            <UserMessage enabled={type === 'IS_REP_ENABLED'} debug='IS_REP_ENABLED'
              title={intl.formatMessage({ id: 'gamut.forms.filling.authenticated_and_welcome' }, { userName })}
              description={intl.formatMessage({ id: 'gamut.forms.filling.proceed_to_form' }, { userName })} />

            {/** OK */}
            <UserMessage enabled={type === 'IS_ANON_FORM_ENABLED'} debug='IS_ANON_FORM_ENABLED'
              title={intl.formatMessage({ id: 'gamut.forms.filling.anonUser_and_welcome' })}
              description={intl.formatMessage({ id: 'gamut.forms.filling.proceed_to_form' }, { userName })} />

            {/** OK */}
            <UserMessage enabled={type === 'IS_USER_FORM_ENABLED'} debug='IS_USER_FORM_ENABLED'
              title={intl.formatMessage({ id: 'gamut.forms.filling.authenticated_and_welcome' }, { userName })}
              description={intl.formatMessage({ id: 'gamut.forms.filling.proceed_to_form' }, { userName })} />


            {/* Alert with red box */}

            {/** OK */}
            <UserMessage error={true} enabled={type === 'IS_ANON_FORM_DISABLED'} debug='IS_ANON_FORM_DISABLED'
              title={intl.formatMessage({ id: 'gamut.forms.filling.must_be_authenticated' })} />

            {/** OK */}
            <UserMessage error={true} enabled={type === 'IS_REP_DISABLED'} debug='IS_REP_DISABLED'
              title={intl.formatMessage({ id: 'gamut.forms.filling.representativeNotAuthorized' })} />

            <UserMessage error={true} enabled={type === 'IS_USER_FORM_DISABLED' || type === 'IS_FORM_DISABLED'} debug='IS_USER_FORM_DISABLED || IS_FORM_DISABLED'
              title={intl.formatMessage({ id: 'gamut.forms.filling.not_available', defaultMessage: 'Form not available at this time' })} />

          </ListItemText>
        </ListItem>
      </List>
    </div>
  )
}


const UserMessage: React.FC<{

  title: React.ReactNode;
  description?: React.ReactNode;
  error?: true;
  debug?: string;
  enabled: boolean;
}> = (props) => {

  const { debug } = props;
  const debugEnabled = true;
  const classes = useUtilityClasses();
  if (!props.enabled) {
    return (
      <>
        {debugEnabled ? <> disabled: {debug}<br /></> : <></>}
      </>);
  }

  return (
    <>
      {debugEnabled ? <> enabled: {debug}<br /></> : <></>}
      <Alert severity={props.error === true ? 'error' : 'success'} variant='filled' className={classes.loginAlert}>
        <AlertTitle>
          {props.title}
        </AlertTitle>
        {props.description}
      </Alert>
    </>)
}


/*


  {allowed ? (
    <Alert severity='success' variant='filled' className={classes.loginAlert}>
      <AlertTitle>
        {anonUser
          ? intl.formatMessage({ id: 'gamut.forms.filling.anonUser_and_welcome' })
          : intl.formatMessage({ id: 'gamut.forms.filling.authenticated_and_welcome' }, { userName })}
      </AlertTitle>
      {intl.formatMessage({ id: 'gamut.forms.filling.proceed_to_form' }, { userName })}
    </Alert>
  ) : notAuthorizedRepresentative ? (
    <Alert severity='error' variant='filled' className={classes.loginAlert}>
        {intl.formatMessage({
          id: 'gamut.forms.filling.representativeNotAuthorized',
        })}
    </Alert>
  ) : (
    <Alert severity='error' variant='filled' className={classes.loginAlert}>
      <Typography className={classes.productBodyTextError}>
        {intl.formatMessage({ id: 'gamut.forms.filling.must_be_authenticated' })}
      </Typography>
    </Alert>
  )}

Alert with green box

1. 'IS_REP_ENABLED'
  title: gamut.forms.filling.authenticated_and_welcome
  body: 'gamut.forms.filling.proceed_to_form

2. 'IS_ANON_FORM_ENABLED'
  title: gamut.forms.filling.anonUser_and_welcome
  body: 'gamut.forms.filling.proceed_to_form'

3. 'IS_USER_FORM_ENABLED'
  title: gamut.forms.filling.authenticated_and_welcome
  body: 'gamut.forms.filling.proceed_to_form




Alert with red box

1. 'IS_ANON_FORM_DISABLED'
  title: gamut.forms.filling.must_be_authenticated
  body: ''

2. 'IS_REP_DISABLED' 
  title: 'gamut.forms.filling.representativeNotAuthorized'
  body: ''

3. 'IS_FORM_DISABLED'
  title: '' ?? 
  body: '' ??

4. 'IS_USER_FORM_DISABLED' 
title: '' ??
body: '' ??

*/
