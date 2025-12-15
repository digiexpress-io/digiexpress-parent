
import { useCardConfig } from '../ledger-card';
import React from 'react';


export type FactoryCardId = 'ledger_main' | 'ledger_payments' | 'ledger_money_requests' | 'ledger_black_books';

export const LEDGER_CARD_IDS: FactoryCardId[] = [
  'ledger_main',
  'ledger_payments',
  'ledger_money_requests',
  'ledger_black_books'
];

export interface LedgerCardContextType {
  id: FactoryCardId;
  isFlashy: boolean;
  isExpanded: boolean;
  isEditOpen: boolean;

  onEditOpen: () => void;
  onEditClose: () => void;
  onToggleFlashy: () => void;
  onToggleExpanded: () => void;
}
export const LedgerCardContext = React.createContext<LedgerCardContextType | undefined>(undefined);

export const defaultExpandedCards: FactoryCardId[] = ['ledger_main', 'ledger_payments', 'ledger_money_requests', 'ledger_black_books'];


export interface LedgerCardProviderProps {
  cardId: FactoryCardId;
  children: React.ReactNode;
}
export const LedgerCardProvider: React.FC<LedgerCardProviderProps> = (props) => {
  const { cardId } = props;
  const {
    editingCardId,
    isCardFlashy, toggleCardFlashy,
    isCardExpanded, toggleCardExpanded, expandedCards,
    setEditCard
  } = useCardConfig();

  const isEditOpen = cardId === editingCardId;

  function onEditOpen() {
    setEditCard(cardId);
  }
  function onEditClose() {
    setEditCard(undefined);
  }

  const contextValue: LedgerCardContextType = {
    id: cardId, 
    isEditOpen,
    isFlashy: isCardFlashy(cardId),
    isExpanded: expandedCards.find(target => target.cardId === cardId) ? isCardExpanded(cardId) : defaultExpandedCards.includes(cardId),
    
    onEditOpen,
    onEditClose,

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
  };

  return (<LedgerCardContext.Provider value={contextValue}>{props.children}</LedgerCardContext.Provider>);
}

export function useLedgerCard() {
  const ctx = React.useContext(LedgerCardContext);
  if (!ctx) {
    throw new Error('Missing ledger card context!');
  }
  return ctx;
}
