import React from 'react';
import { useIntl } from 'react-intl';
import { EditOutlined as EditOutlinedIcon } from '@mui/icons-material';


import { CardId, LedgerCardDataRowText, StartAdornmentIcon, useCardConfig, useCardThemeConfig } from '../ledger-card';
import { useLedger } from '@dxs-ts/ledger-api';
import { LedgerCard } from '../ledger-card';


export type FactoryCardId = 'ledger_main';

export const LEDGER_CARD_IDS: FactoryCardId[] = [
  'ledger_main',
];

const defaultExpandedCards: FactoryCardId[] = ['ledger_main'];

export const LedgerCardFactory: React.FC<{ cardId: CardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: FactoryCardId = initProps.cardId as FactoryCardId;
  const { ledgerContainer } = useLedger();
  const { ledger, payments } = ledgerContainer;

  //console.log(ledger, payments)


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
    case 'ledger_main': {
      return (
        <LedgerCard title={intl.formatMessage({ id: 'ledgercard.title.ledgerMain' }, { ledgerName: ledger.name })}
          {...commonProps}
          isMenu
          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={true}
          onEdit={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={EditOutlinedIcon} />}
          onDoubleClick={handleEdit}
        //editDialog={isEditOpen && (<CustomerMessagesEditDialog open onClose={handleEditClose} />)}
        >
          <LedgerCardDataRowText label={intl.formatMessage({ id: 'ledgercard.ledgerMain.description' })} value={ledger.description} style={style} />
        </LedgerCard>
      );
    }

    default: return null;
  }
}
