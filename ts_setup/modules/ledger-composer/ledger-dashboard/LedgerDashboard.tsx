import React from 'react';
import { Grid2, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useLedger } from '@dxs-ts/ledger-api';

import { LedgerCardFactory, FactoryCardId } from '../ledger-card-factory';
import { 
  CardConfigContextProvider, 
  cardGridSize, 
  DraggableCardWrapper, 
  useCardConfig, useCardThemeConfig, useDragCardController
} from '../ledger-card';


const _variant_prod: FactoryCardId[] = [
  'ledger_main',
  'ledger_payments',
  'ledger_money_requests',
  'ledger_black_books'
];


const DashboardInternal: React.FC = () => {
  const intl = useIntl();
  const { cardOrder, isReviewOpen, cardTheme } = useCardConfig();
  const { ledgerContainer } = useLedger();
  const styleConfig = useCardThemeConfig();
  const { getDragPropsForId, draggingId } = useDragCardController();

  const style = styleConfig[cardTheme];

  console.log(ledgerContainer)

  return (
    <Grid2 container spacing={style.cardSpacing} m={1}>
      <Grid2>
        <Typography variant='h1'>
          {intl.formatMessage({ id: 'ledger.composer.contract.edit', defaultMessage: 'Edit ledger' })}
          {intl.formatMessage({ id: 'eveli.textSeparator', defaultMessage: ': ' })}
          {ledgerContainer.ledger.name}
        </Typography>
      </Grid2>

      <Grid2 container
        size={{ xs: 12, md: isReviewOpen ? 6 : 12 }}
        spacing={style.cardSpacing}
        sx={{ overflowY: 'auto', maxHeight: '100%', overflow: 'visible' }}>
        {cardOrder.map((cardId) => (
          <Grid2 key={cardId} size={isReviewOpen ? cardGridSize.singleCol : cardGridSize[cardTheme]}>
            <DraggableCardWrapper {...getDragPropsForId(cardId)} draggingId={draggingId}>
              <LedgerCardFactory cardId={cardId} />
            </DraggableCardWrapper>
          </Grid2>
        ))}
      </Grid2>
    </Grid2>
  );
};

export const LedgerDashboard: React.FC = () => {

  const initialCardOrder: FactoryCardId[] = React.useMemo(() => {

    return [
      ..._variant_prod,
    ];

  }, [])


  return (
    <CardConfigContextProvider cardTheme='large' initialCardOrder={initialCardOrder}>
      <DashboardInternal />
    </CardConfigContextProvider>
  );
}
