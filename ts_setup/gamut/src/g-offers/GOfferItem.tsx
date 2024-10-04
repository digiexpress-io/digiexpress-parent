import React from 'react';
import { Button, Grid, Typography, useThemeProps } from '@mui/material';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';

import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';
import { GConfirm, GDate, GDateProps, GFlex } from '../';
import { GOfferItemRoot, useUtilityClasses, MUI_NAME } from './useUtilityClasses';



export interface GOfferItemProps {
  name: string;
  created: DateTime;
  updated: DateTime;
  offerId: string;
  onOpen: (offerId: string) => void;
  onCancel: (offerId: string) => void;
  slotProps?: {
    date?: Partial<GDateProps>
  }
}


export const GOfferItem: React.FC<GOfferItemProps> = (initProps) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  const [confirmOpen, setConfirmOpen] = React.useState(false);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { created, updated, name, slotProps = {} } = props;
  const ownerState = {
    ...props,
    dateVariant: slotProps.date?.variant ?? 'relative'
  }


  function handleCancelConfirm() {
    setConfirmOpen(prev => !prev)
  }


  return (<>
    <GConfirm
      open={confirmOpen}
      onClose={handleCancelConfirm}
      cancelItemName={props.name}
      cancelItemMeta={<>
        {intl.formatMessage({ id: 'gamut.forms.lastModified' })}
        {intl.formatMessage({ id: 'gamut.textSeparator' })}
        <GDate variant='relative' date={props.updated} />
      </>
      }
      title={intl.formatMessage({ id: 'gamut.offers.deleteForm.title' })}
      content={intl.formatMessage({ id: 'gamut.offers.deleteForm.content' })}
      cancelButton={intl.formatMessage({ id: 'gamut.offers.deleteForm.cancelButton' })}
      deleteButton={intl.formatMessage({ id: 'gamut.offers.deleteForm.deleteButton' })}
    />
    <GOfferItemRoot className={classes.root} ownerState={ownerState}>
      <GFlex variant='body'>
        <Grid container>
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
            <Button startIcon={<DeleteForeverIcon />} className={classes.cancel} onClick={handleCancelConfirm}>
              <Typography>{intl.formatMessage({ id: 'gamut.buttons.cancel' })}</Typography>
            </Button>
          </Grid>
        </Grid>
      </GFlex>
    </GOfferItemRoot>
  </>
  )
}




