import React from 'react';
import { Alert, Grid, Typography, useThemeProps } from '@mui/material';
import { useIntl } from 'react-intl';
import { GFlex, OfferApi, useOffers } from '../';
import { GOffersRoot, useUtilityClasses, MUI_NAME } from './useUtilityClasses';
import { GOfferItemProps, GOfferItem } from './GOfferItem';
import { GOverridableComponent } from '../g-override';


export interface GOffersProps {
  slotProps?: {
    item?: Partial<GOfferItemProps>
  },
  slots?: {
    item?: React.ElementType<GOfferItemProps>
  }
  component?: GOverridableComponent<GOffersProps>;
}


export const GOffers: React.FC<GOffersProps> = (initProps) => {
  const intl = useIntl();
  const { offers, cancelOffer } = useOffers();
  const classes = useUtilityClasses();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const ownerState = {
    ...props
  }


  const Item: React.ElementType<GOfferItemProps> = props.slots?.item ?? GOfferItem;
  function mapToItem(offer: OfferApi.Offer): GOfferItemProps & { key: string } {
    return {
      key: offer.id,
      created: offer.created,
      updated: offer.updated,
      name: offer.name,
      offerId: offer.id,
      onOpen: props.slotProps?.item?.onOpen ?? ((offer: OfferApi.Offer) => { console.log("Do nothing on offer", offer) }),
      onCancel: props.slotProps?.item?.onCancel ?? cancelOffer,
      ...(props.slotProps?.item ?? {}),
    };
  }
  const Root = props.component ?? GOffersRoot;

  return (
    <Root className={classes.root} ownerState={ownerState}>
      {offers.length ? (
        <GFlex variant='header'>
          <Grid container>
            <Grid item lg={4}><Typography className={classes.header}>{intl.formatMessage({ id: 'gamut.forms.formName' })}</Typography></Grid>
            <Grid item lg={3}><Typography className={classes.header}>{intl.formatMessage({ id: 'gamut.forms.started' })}</Typography></Grid>
            <Grid item lg={3}><Typography className={classes.header}>{intl.formatMessage({ id: 'gamut.forms.lastModified' })}</Typography></Grid>
          </Grid>
        </GFlex>
      ) : (
        <Alert severity='info' className={classes.noOffers}>{intl.formatMessage({ id: 'gamut.forms.unfinished.none' })}</Alert>)
      }

      {offers.map(mapToItem).map((offer) => (
        <Item {...offer} />
      ))}
    </Root>)
}

