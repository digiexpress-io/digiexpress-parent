import React from 'react';
import { Button, Grid, Typography, useThemeProps } from '@mui/material';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';

import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { GOfferItemRoot, useUtilityClasses, MUI_NAME } from './useUtilityClasses';
import { OfferApi, useOffers } from '@dxs-ts/gamut-api';
import { GDate, GDateProps } from '../g-date';
import { GConfirm } from '../g-confirm';
import { GFlex } from '../g-flex';


export interface GOfferItemProps {
  name: string;
  created: DateTime;
  updated: DateTime;
  offerId: string;
  onOpen: (offer: OfferApi.Offer) => void;
  onCancel: (offerId: string) => void;
  slotProps?: {
    date?: Partial<GDateProps>
  }
}


export const GOfferItem: React.FC<GOfferItemProps> = (initProps) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const offers = useOffers();
  const [confirmOpen, setConfirmOpen] = React.useState(false);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { created, updated, name, offerId, slotProps = {}, onOpen } = props;
  const ownerState = {
    ...props,
    dateVariant: slotProps.date?.variant ?? 'date-only'
  }


  function handleToggleDialog() {
    setConfirmOpen(prev => !prev)
  }

  function handleDeleteOffer(offerId: string) {
    offers.cancelOffer(offerId);
    setConfirmOpen(prev => !prev);
  }

  return (<>
    <GConfirm
      open={confirmOpen}
      onClose={handleToggleDialog}
      onDelete={() => handleDeleteOffer(offerId)}
      cancelItemName={props.name}
      cancelItemMeta={<>
        {intl.formatMessage({ id: 'gamut.forms.lastModified' })}
        {intl.formatMessage({ id: 'gamut.textSeparator' })}
        <GDate variant='date-only' date={props.updated} />
      </>
      }
      title={intl.formatMessage({ id: 'gamut.offers.deleteForm.title' })}
      content={intl.formatMessage({ id: 'gamut.offers.deleteForm.content' })}
      cancelButton={intl.formatMessage({ id: 'gamut.offers.deleteForm.cancelButton' })}
      deleteButton={intl.formatMessage({ id: 'gamut.offers.deleteForm.deleteButton' })}
    />
    <GOfferItemRoot className={classes.root} ownerState={ownerState}>
      <GFlex variant='body'>
        <Grid container onClick={() => onOpen(offers.getOffer(offerId)!)}>
          <Grid item xs={12} sm={12} md={12} lg={5} xl={4}>
            <Typography>{name}</Typography>
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={2} xl={3}>
            <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
              <Typography component='span' className={classes.started}>
                {intl.formatMessage({ id: 'gamut.forms.started' })}
              </Typography>
            </GFlex>
            <Typography component='span'>
              <GDate variant={ownerState.dateVariant} date={created} />
            </Typography>
          </Grid>

          <Grid item xs={12} sm={12} md={12} lg={3} xl={3}>
            <GFlex variant='hidden' hiddenOn={(br) => br.up('lg')}>
              <Typography component='span' className={classes.lastModified}>
                {intl.formatMessage({ id: 'gamut.forms.lastModified' })}
              </Typography>
            </GFlex>
            <Typography component='span'>
              <GDate variant={ownerState.dateVariant} date={updated} />
            </Typography>
          </Grid>


          <Grid item xs={12} sm={12} md={12} lg={2} xl={2}>
            <Button startIcon={<DeleteForeverIcon />} className={classes.cancel}
              onClick={(event) => {
                event.stopPropagation(); // prevent clicking the grid from overriding the button click
                handleToggleDialog();
              }}>
              <Typography>{intl.formatMessage({ id: 'gamut.buttons.cancel' })}</Typography>
            </Button>
          </Grid>
        </Grid>
      </GFlex>
    </GOfferItemRoot>
  </>
  )
}




