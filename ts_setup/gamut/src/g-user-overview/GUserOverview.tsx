import React from 'react';
import { useThemeProps, Typography, Grid } from '@mui/material';
import { useIntl } from 'react-intl';
import { useNavigate } from '@tanstack/react-router';

import { GUserOverviewDetail, GUserOverviewDetailProps } from './GUserOverviewDetail';
import { GUserOverviewMenuView } from '../';
import { useUtilityClasses, GUserOverviewRoot, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GUserOverviewProps {
  userName: string;
  userAddress: string;
  userCityAndCountry: string;
  userZipcode: string;

  startedForms: number,
  waitingForms: number,
  decidedForms: number,
  newMessages: number,
  topicCount: number,
  bookings: number,

  slots?: {
    item: React.ElementType<GUserOverviewDetailProps>
  };
  component?: GOverridableComponent<GUserOverviewProps>
}


export const GUserOverview: React.FC<GUserOverviewProps> = (initProps) => {
  const nav = useNavigate();
  const intl = useIntl();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses();
  const ownerState = {
    ...props
  }

  const Item = props.slots?.item ?? GUserOverviewDetail;

  function handleClick(viewId: GUserOverviewMenuView | undefined) {
    if (!viewId) {
      return;
    }

    nav({
      from: '/secured/$locale/views/$viewId',
      params: { viewId },
      to: '/secured/$locale/views/$viewId',
    })
  }

  const Root = initProps.component ?? GUserOverviewRoot;

  return (
    <Root ownerState={ownerState} className={classes.root}>
      <Grid container spacing={1} height='100%' width='75%'>

        <Grid item xs={12} sm={12} md={6} lg={6} xl={6}>
          <Item title={props.userName}>
            <Typography>{props.userAddress}</Typography>
            <Typography>{props.userCityAndCountry}</Typography>
            <Typography>{props.userZipcode}</Typography>
          </Item>
        </Grid>

        <Grid item xs={12} sm={12} md={6} lg={6} xl={6} className={classes.serviceSelect}>
          <Item
            viewId='service-select'
            onClick={handleClick}
            title={intl.formatMessage({ id: 'gamut.userOverview.services.detail.title' })}
            buttonLabel={intl.formatMessage({ id: 'gamut.services' })}
            count={props.topicCount}
          />
        </Grid>

        <Grid item xs={12} sm={12} md={6} lg={6} xl={6}>
          <Item 
            viewId='requests-in-progress'
            onClick={handleClick}
            title={intl.formatMessage({ id: 'gamut.userOverview.unfinished-forms.detail.title' })}
            buttonLabel={intl.formatMessage({ id: 'gamut.forms' })}
            count={props.startedForms}
          />
        </Grid>

        <Grid item xs={12} sm={12} md={6} lg={6} xl={6}>
          <Item 
            onClick={handleClick}
            viewId='awaiting-decision'
            title={intl.formatMessage({ id: 'gamut.userOverview.waiting-forms.detail.title' })}
            buttonLabel={intl.formatMessage({ id: 'gamut.forms' })}
            count={props.waitingForms}
          />
        </Grid>

        <Grid item xs={12} sm={12} md={6} lg={6} xl={6}>
          <Item
            onClick={handleClick}
            viewId='with-decision'
            title={intl.formatMessage({ id: 'gamut.userOverview.decided-forms.detail.title' })}
            buttonLabel={intl.formatMessage({ id: 'gamut.forms' })}
            count={props.decidedForms}
          />
        </Grid>

        <Grid item xs={12} sm={12} md={6} lg={6} xl={6}>
          <Item
            onClick={handleClick}
            viewId='inbox'
            title={intl.formatMessage({ id: 'gamut.userOverview.inbox.detail.title' })}
            buttonLabel={intl.formatMessage({ id: 'gamut.new-messages' })}
            count={props.newMessages}
          />
        </Grid>

        <Grid item xs={12} sm={12} md={6} lg={6} xl={6}>
          <Item
            onClick={handleClick}
            viewId='bookings'
            title={intl.formatMessage({ id: 'gamut.userOverview.bookings.detail.title' })}
            buttonLabel={intl.formatMessage({ id: 'gamut.bookings' })}
            count={props.bookings}
          />
        </Grid>

      </Grid>
    </Root>
  )
}
