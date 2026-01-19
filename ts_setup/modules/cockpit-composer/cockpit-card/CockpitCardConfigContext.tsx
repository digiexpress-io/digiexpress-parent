import React, { createContext, PropsWithChildren, useContext } from 'react';

export type CockpitCardId = string;
export type CockpitCardStyleKey = 'compact' | 'default' | 'large';

export interface CockpitCardConfig {
  isReviewOpen: boolean;
  editingCardId: string | undefined;
  cardTheme: CockpitCardStyleKey;
  cardOrder: string[];
  expandedCards: { cardId: string, expanded: boolean }[];

  isCardExpanded(id: string): boolean;
  toggleCardExpanded(id: string, forceToExpandedValue?: boolean): void;

  toggleReview(): void;
  isCardFlashy(id: string): boolean;
  toggleCardFlashy(id: string): void;

  setEditCard(id: string | undefined): void;
  setCardOrder(newOrder: string[]): void;
  setCardTheme(theme: CockpitCardStyleKey): void;
}

const INITIAL_CONFIG: CockpitCardConfig = {
} as any;

export interface CockpitCardConfigContextProviderProps {
  cardTheme?: CockpitCardStyleKey;
  initialCardOrder: string[];
}

export const CockpitCardConfigContext = createContext<CockpitCardConfig>(INITIAL_CONFIG);

export const CockpitCardConfigContextProvider: React.FC<PropsWithChildren<CockpitCardConfigContextProviderProps>> = (props) => {
  const [isReviewOpen, setReviewOpen] = React.useState(false);

  const [flashyCards, setFlashyCards] = React.useState<string[]>([]);
  const [expandedCards, setExpandedCards] = React.useState<CockpitCardConfig['expandedCards']>([]);
  const [cardOrder, setCardOrder] = React.useState<string[]>(props.initialCardOrder);
  const [editingCardId, setEditingCardId] = React.useState<string | undefined>();
  const [cardTheme, setCardTheme] = React.useState<CockpitCardStyleKey>(props.cardTheme ?? 'default');

  const contextValue: CockpitCardConfig = React.useMemo(() => {
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
    <CockpitCardConfigContext.Provider value={contextValue}>
      {props.children}
    </CockpitCardConfigContext.Provider>
  );
};

export const useCockpitCardConfig = () => useContext(CockpitCardConfigContext);