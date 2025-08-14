
import React, { createContext, PropsWithChildren, useContext } from 'react';



export type TaskCardId =
  'task_main' |
  'task_form_summary' |
  'status_priority' |
  'assignees_roles' |
  'customer_messages'|
  'files'|
  'feedback' |
  'notes'|
  'task_meta'


export const TASK_CARD_IDS: TaskCardId[] = [
  'task_main',
  'task_form_summary',
  'status_priority',
  'assignees_roles',
  'customer_messages',
  'files',
  'feedback',
  'notes',
  'task_meta'
];

export type TaskCardStyleKey = 'compact' | 'default' | 'large';

export interface CardConfig {
  isReviewOpen: boolean;
  editingCardId: TaskCardId | undefined;
  cardTheme: TaskCardStyleKey;
  cardOrder: TaskCardId[];

  toggleReview(): void;
  isCardFlashy(id: TaskCardId): boolean;
  toggleCardFlashy(id: TaskCardId): void;

  isCardAltView(id: TaskCardId): boolean;
  toggleCardAltView(id: TaskCardId): void;

  setEditCard(id: TaskCardId | undefined): void;
  setCardOrder(newOrder: TaskCardId[]): void;
  setCardTheme(theme: TaskCardStyleKey): void;
}

const INITIAL_CONFIG: CardConfig = {
} as any;

export interface CardConfigContextProviderProps {

}

export const CardConfigContext = createContext<CardConfig>(INITIAL_CONFIG);

export const CardConfigContextProvider: React.FC<PropsWithChildren<CardConfigContextProviderProps>> = ({ children }) => {
  const [isReviewOpen, setReviewOpen] = React.useState(false);

  const [flashyCards, setFlashyCards] = React.useState<TaskCardId[]>([]);
  const [altViewCards, setAltViewCards] = React.useState<TaskCardId[]>([]);

  const [editingCardId, setEditingCardId] = React.useState<TaskCardId | undefined>();
  const [cardTheme, setCardTheme] = React.useState<TaskCardStyleKey>('default');
  const [cardOrder, setCardOrder] = React.useState<TaskCardId[]>(TASK_CARD_IDS);


  const contextValue: CardConfig = React.useMemo(() => {
    return {
      cardTheme, isReviewOpen, editingCardId, cardOrder,
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
      isCardAltView(cardId) {
        return altViewCards.includes(cardId);
      },
      toggleCardAltView(cardId) {
        setAltViewCards(prev => prev.includes(cardId) ? prev.filter(id => cardId !== id) : [...prev, cardId])
      },
    }
  }, [isReviewOpen, editingCardId, cardTheme, flashyCards, altViewCards, cardOrder]);


  return (
    <CardConfigContext.Provider value={contextValue}>
      {children}
    </CardConfigContext.Provider>
  );
};

export const useCardConfig = () => useContext(CardConfigContext);
