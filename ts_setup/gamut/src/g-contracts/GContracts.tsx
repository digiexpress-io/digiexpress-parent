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
  const props = useThemeProps({ props: initProps, name: MUI_NAME });
  const classes = useUtilityClasses();

  const { contracts } = useContracts();
  const { site } = useSite();
  const { getLocalisedOfferName } = useOffers();
  const { getSubject } = useComms();

  const Item: React.ElementType<GContractItemProps> = props.slots?.item ?? GContractItem;

  function mapToItem(contract: ContractApi.Contract): GContractItemProps & { id: string } {
    const offerName = getLocalisedOfferName(site!, contract.offer.name);
    const subject = getSubject(contract.exchangeId);
    const hasUnviewedMessages = subject?.isViewed === false;

    return {
      id: contract.id,
      referenceId: contract.referenceId,
      exchangeId: contract.exchangeId,
      name: offerName,
      lastModified: contract.updated!,
      status: contract.status,
      documents: contract.documents.length,
      messages: getSubject(contract.exchangeId)?.exchange.length ?? 0,
      hasUnviewedMessages,
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
          <Grid item lg={3} xl={3}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.formName' })}</Typography></Grid>
          <Grid item lg={2} xl={2}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.taskRefId' })}</Typography></Grid>
          <Grid item lg={2} xl={2}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.status' })}</Typography></Grid>
          <Grid item lg={1} xl={1}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.files' })}</Typography></Grid>
          <Grid item lg={1} xl={1}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.messages' })}</Typography></Grid>
          <Grid item lg={3} xl={3}><Typography fontWeight='bold'>{intl.formatMessage({ id: 'gamut.forms.lastModified' })}</Typography></Grid>
        </Grid>
      </GFlex>

      {contracts
        .filter(props.filter)
        .map((contract) => {
          const subject = getSubject(contract.exchangeId);
          const hasUnviewedMessages = subject?.isViewed === false;
          const lastMsgDate = subject?.lastExchange?.created ?? subject?.created;
          return {
            contract,
            hasUnviewedMessages,
            lastMsgDate: lastMsgDate?.toMillis?.() ?? 0,
          };
        })
        .sort((a, b) => {
          if (a.hasUnviewedMessages !== b.hasUnviewedMessages) {
            return a.hasUnviewedMessages ? -1 : 1;
          }
          if (b.lastMsgDate !== a.lastMsgDate) {
            return b.lastMsgDate - a.lastMsgDate;
          }
        
          const aUpdated = a.contract.updated?.toMillis?.() ?? 0;
          const bUpdated = b.contract.updated?.toMillis?.() ?? 0;
          return bUpdated - aUpdated;
        })        
        .map(({ contract }) => mapToItem(contract))
        .map((contract) => (
          <Item key={contract.id} {...contract} />
        ))}
    </Root>
  );
};
