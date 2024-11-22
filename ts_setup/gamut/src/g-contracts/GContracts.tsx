import React from 'react';
import { Grid, Typography, useThemeProps } from '@mui/material';
import { useIntl } from 'react-intl';
import { GFlex } from '../g-flex';
import { ContractApi, useContracts } from '../api-contract';
import { GContractsRoot, useUtilityClasses, MUI_NAME } from './useUtilityClasses';
import { GContractItem, GContractItemProps } from './GContractItem';
import { useComms } from '../api-comms';
import { GOverridableComponent } from '../g-override';
import { useOffers } from '../api-offer';
import { useSite } from '../api-site';


export interface GContractsProps {
  filter: (contract: ContractApi.Contract) => boolean;
  component?: GOverridableComponent<GContractsProps>;

  slotProps?: {
    item?: Partial<GContractItemProps>
  },
  slots?: {
    item?: React.ElementType<GContractItemProps>
  }
}


export const GContracts: React.FC<GContractsProps> = (initProps) => {
  const intl = useIntl();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const { contracts } = useContracts();
  const { site } = useSite();
  const { getLocalisedOfferName } = useOffers();
  const { getSubject } = useComms();

  const Item: React.ElementType<GContractItemProps> = props.slots?.item ?? GContractItem;

  function mapToItem(contract: ContractApi.Contract): GContractItemProps & { key: string } {

    const offerName = getLocalisedOfferName(site!, contract.offer.name);

    return {
      key: contract.id,
      exchangeId: contract.exchangeId,
      color: props.slotProps?.item?.color,
      name: offerName,
      lastModified: contract.updated!,
      status: contract.status,
      documents: contract.documents.length,
      messages: getSubject(contract.exchangeId)?.exchange.length ?? 0,
      onClick: (exchangeId) => props.slotProps?.item?.onClick ? props.slotProps.item.onClick(exchangeId) : () => {},
      slotProps: { ...(props.slotProps?.item ?? {}) },

      ...(props.slotProps?.item ?? {})
    };
  }
  const Root = props.component ?? GContractsRoot

  return (
    <Root className={classes.root} ownerState={props}>
      <GFlex variant='header'>
        <Grid container>
          <Grid item lg={4} xl={4}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.formName' })}</Typography></Grid>
          <Grid item lg={3} xl={3}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.status' })}</Typography></Grid>
          <Grid item lg={1} xl={1}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.files' })}</Typography></Grid>
          <Grid item lg={1} xl={1}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.messages' })}</Typography></Grid>
          <Grid item lg={3} xl={3}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.lastModified' })}</Typography></Grid>
        </Grid>
      </GFlex>


      {contracts
        .filter(props.filter)
        .map(mapToItem)
        .map(contract => (<Item {...contract} />))}
    </Root>)
}


