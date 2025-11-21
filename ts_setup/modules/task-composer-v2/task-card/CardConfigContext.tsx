import React, { PropsWithChildren } from 'react';
import { TaskApi } from '@dxs-ts/task-api';

export type TaskCardId = string;
export type TaskCardStyleKey = 'compact' | 'default' | 'large';

export interface CardConfig {
  isReviewOpen: boolean;
  reviewAssignment: TaskApi.TaskCustomerAssignment | undefined;
  editingCardId: string | undefined;
  cardTheme: TaskCardStyleKey;
  cardOrder: string[];
  expandedCards: { cardId: string, expanded: boolean }[];
  
  isCardExpanded(id: string): boolean;
  toggleCardExpanded(id: string, forceToExpandedValue?: boolean): void;

  toggleReview(option?: TaskApi.TaskCustomerAssignment): void;
  isCardFlashy(id: string): boolean;
  toggleCardFlashy(id: string): void;


  setEditCard(id: string | undefined): void;
  setCardOrder(newOrder: string[]): void;
  setCardTheme(theme: TaskCardStyleKey): void;
}

export interface CardConfigContextProviderProps {
  cardTheme?: TaskCardStyleKey;
  initialCardOrder: string[];
}

export const CardConfigContext = React.createContext<CardConfig | undefined>(undefined);

export const CardConfigContextProvider: React.FC<PropsWithChildren<CardConfigContextProviderProps>> = (props) => {
  const [reviewOpen, setReviewOpen] = React.useState<{ open: boolean, option: TaskApi.TaskCustomerAssignment | undefined }>({ open: false, option: undefined });

  const [flashyCards, setFlashyCards] = React.useState<string[]>([]);
  const [expandedCards, setExpandedCards] = React.useState<CardConfig['expandedCards']>([]);
  const [cardOrder, setCardOrder] = React.useState<string[]>(props.initialCardOrder);
  const [editingCardId, setEditingCardId] = React.useState<string | undefined>();
  const [cardTheme, setCardTheme] = React.useState<TaskCardStyleKey>(props.cardTheme ?? 'default');

  const contextValue: CardConfig = React.useMemo(() => {
    return {
      cardTheme, editingCardId, cardOrder, expandedCards,

      isReviewOpen: reviewOpen.open,
      reviewAssignment: reviewOpen.option,

      setCardOrder, setCardTheme,
      toggleReview(option?: TaskApi.TaskCustomerAssignment) {
        setReviewOpen(prev => ({ open: !prev.open, option }))
      },
      isCardFlashy(cardId) {
        return flashyCards.includes(cardId)
      },
      toggleCardFlashy(cardId) {
        setFlashyCards(prev => prev.includes(cardId) ? prev.filter(id => cardId !== id) : [...prev, cardId])
      },
      setEditCard(cardId) {
        setEditingCardId(cardId)
      },
      isCardExpanded(cardId) {
        return expandedCards.find(c => c.cardId === cardId)?.expanded ?? false;
      },
      toggleCardExpanded(cardId, forceToExpandedValue) {
        setExpandedCards(prev => {
          const targetCard = expandedCards.find(c => c.cardId === cardId);
          if(targetCard) {
            return [...prev.filter(card => card.cardId !== cardId), { cardId, expanded: forceToExpandedValue ?? !targetCard.expanded} ]
          } 
          return [...prev, { cardId, expanded: forceToExpandedValue ?? true}];
        });
      }
    }
  }, [reviewOpen.open, reviewOpen.option?.id, editingCardId, cardTheme, flashyCards, expandedCards, cardOrder]);


  return (
    <CardConfigContext.Provider value={contextValue}>
      {props.children}
    </CardConfigContext.Provider>
  );
};

export const useCardConfig = () => {
  const ctx = React.useContext(CardConfigContext);
  if (!ctx) {
    throw new Error('Missing task card context!');
  }
  return ctx;
}
