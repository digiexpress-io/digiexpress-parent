import React from 'react';
import { CardId } from '../ledger-card';

import { LedgerCardFactoryMapping } from './LedgerCardFactoryMapping';
import { FactoryCardId, LedgerCardProvider } from './LedgerCardProvider';



export const LedgerCardFactory: React.FC<{ cardId: CardId }> = (initProps) => {
  const cardId: FactoryCardId = initProps.cardId as FactoryCardId;

  return (<LedgerCardProvider cardId={cardId}>
    <LedgerCardFactoryMapping />
  </LedgerCardProvider>)
}


