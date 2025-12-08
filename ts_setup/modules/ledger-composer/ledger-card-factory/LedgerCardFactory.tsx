import React from 'react';
import { useIntl } from 'react-intl';


import { CardId, useCardConfig, useCardThemeConfig } from '../ledger-card';
import { useLedger } from '@dxs-ts/ledger-api';


export type FactoryCardId = 'ledger_main';

export const CONTRACT_CARD_IDS: FactoryCardId[] = [
  'ledger_main',
];

const defaultExpandedCards: FactoryCardId[] = ['ledger_main'];

export const ContractCardFactory: React.FC<{ cardId: CardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: FactoryCardId = initProps.cardId as FactoryCardId;
  const { ledgerContainer } = useLedger();
  const {  } = ledgerContainer;

  const {
    cardTheme, editingCardId, toggleReview,
    isCardFlashy, toggleCardFlashy, setEditCard,
    isCardExpanded, toggleCardExpanded, expandedCards
  } = useCardConfig();

  const styleConfig = useCardThemeConfig();
  const style = styleConfig[cardTheme];


  const commonProps = {
    id: cardId,
    styleVariant: cardTheme,
    isFlashy: isCardFlashy(cardId),
    isExpanded: expandedCards.find(target => target.cardId === cardId) ? isCardExpanded(cardId) : defaultExpandedCards.includes(cardId),
    onToggleFlashy: () => toggleCardFlashy(cardId),
    onToggleExpanded: () => {
      const current = expandedCards.find(target => target.cardId === cardId);
      const isDefault = defaultExpandedCards.includes(cardId)
      if (isDefault) {
        toggleCardExpanded(cardId, current ? undefined : false);
      } else {
        toggleCardExpanded(cardId, current ? undefined : true);
      }
    },
    onReview: toggleReview,
  };

  const isEditOpen = cardId === editingCardId;

  function handleEdit() {
    setEditCard(cardId);
  }
  function handleEditClose() {
    setEditCard(undefined);
  }


  switch (cardId) {

    default: return null;
  }
}
