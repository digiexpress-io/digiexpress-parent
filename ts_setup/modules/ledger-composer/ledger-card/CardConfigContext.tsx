
import React, { createContext, PropsWithChildren, useContext } from 'react';

export type CardId = string;
export type CardStyleKey = 'compact' | 'default' | 'large';

export interface CardConfig {
  isReviewOpen: boolean;
  editingCardId: string | undefined;
  cardTheme: CardStyleKey;
  cardOrder: string[];
  expandedCards: { cardId: string, expanded: boolean }[];
  
  isCardExpanded(id: string): boolean;
  toggleCardExpanded(id: string, forceToExpandedValue?: boolean): void;

  toggleReview(): void;
  isCardFlashy(id: string): boolean;
  toggleCardFlashy(id: string): void;


  setEditCard(id: string | undefined): void;
  setCardOrder(newOrder: string[]): void;
  setCardTheme(theme: CardStyleKey): void;
}

const INITIAL_CONFIG: CardConfig = {
} as any;

export interface CardConfigContextProviderProps {
  cardTheme?: CardStyleKey;
  initialCardOrder: string[];
}

export const CardConfigContext = createContext<CardConfig>(INITIAL_CONFIG);

export const CardConfigContextProvider: React.FC<PropsWithChildren<CardConfigContextProviderProps>> = (props) => {
  const [isReviewOpen, setReviewOpen] = React.useState(false);

  const [flashyCards, setFlashyCards] = React.useState<string[]>([]);
  const [expandedCards, setExpandedCards] = React.useState<CardConfig['expandedCards']>([]);
  const [cardOrder, setCardOrder] = React.useState<string[]>(props.initialCardOrder);
  const [editingCardId, setEditingCardId] = React.useState<string | undefined>();
  const [cardTheme, setCardTheme] = React.useState<CardStyleKey>(props.cardTheme ?? 'default');


  const contextValue: CardConfig = React.useMemo(() => {
    return {
      cardTheme, isReviewOpen, editingCardId, cardOrder, expandedCards,
      setCardOrder, setCardTheme,
      toggleReview() {
        setReviewOpen(prev => !prev)
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
  }, [isReviewOpen, editingCardId, cardTheme, flashyCards, expandedCards, cardOrder]);


  return (
    <CardConfigContext.Provider value={contextValue}>
      {props.children}
    </CardConfigContext.Provider>
  );
};

export const useCardConfig = () => useContext(CardConfigContext);
