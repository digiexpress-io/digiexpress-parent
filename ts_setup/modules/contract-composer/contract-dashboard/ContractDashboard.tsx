import React from 'react';
import { Grid2, Typography } from '@mui/material';


import { useIntl } from 'react-intl';
import { CardConfigContextProvider, contractCardGridSize, DraggableCardWrapper, useCardConfig, useContractCardThemeConfig, useDragCardController } from '../contract-card';
import { ContractCardFactory, FactoryCardId } from '../contract-card-factory';
import { useContract } from '@dxs-ts/contract-api';

const _variant_prod: FactoryCardId[] = [
  'contract_main',
  'product',
  'contract_parties',
  'coverages',
  'payment_plans',
  'investment_plans',
];


const ContractDashboardInternal: React.FC = () => {
  const intl = useIntl();
  const { cardOrder, isReviewOpen, cardTheme } = useCardConfig();
  const { contractContainer } = useContract();
  const styleConfig = useContractCardThemeConfig();
  const { getDragPropsForId, draggingId } = useDragCardController();

  const style = styleConfig[cardTheme];

  return (
    <Grid2 container spacing={style.cardSpacing} m={1}>
      <Grid2>
        <Typography variant='h1'>
          {intl.formatMessage({ id: 'contract.composer.contract.edit', defaultMessage: 'Edit contract' })}
          {intl.formatMessage({ id: 'eveli.textSeparator', defaultMessage: ': ' })}
          {contractContainer.contract.contractNumber}
        </Typography>
      </Grid2>

      <Grid2 container
        size={{ xs: 12, md: isReviewOpen ? 6 : 12 }}
        spacing={style.cardSpacing}
        sx={{ overflowY: 'auto', maxHeight: '100%', overflow: 'visible' }}>
        {cardOrder.map((cardId) => (
          <Grid2 key={cardId} size={isReviewOpen ? contractCardGridSize.singleCol : contractCardGridSize[cardTheme]}>
            <DraggableCardWrapper {...getDragPropsForId(cardId)} draggingId={draggingId}>
            <ContractCardFactory cardId={cardId} />
            </DraggableCardWrapper>
          </Grid2>
        ))}
      </Grid2>
    </Grid2>
  );
};

export const ContractDashboard: React.FC = () => {

  const initialCardOrder: FactoryCardId[] = React.useMemo(() => {

    return [
      ..._variant_prod,
    ];

  }, [])


  return (
    <CardConfigContextProvider cardTheme='large' initialCardOrder={initialCardOrder}>
      <ContractDashboardInternal />
    </CardConfigContextProvider>
  );
}
