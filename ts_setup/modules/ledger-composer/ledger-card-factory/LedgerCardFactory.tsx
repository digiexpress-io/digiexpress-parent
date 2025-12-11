import React from 'react';
import { useIntl } from 'react-intl';
import { EditOutlined as EditOutlinedIcon } from '@mui/icons-material';
import { CreditCard as CreditCardIcon } from '@mui/icons-material';
import { AccountBalanceWallet as AccountBalanceWalletIcon } from '@mui/icons-material';
import { RequestPageOutlined as RequestPageOutlinedIcon } from '@mui/icons-material';
import { CardId, LedgerCardDataList, LedgerCardDataRowText, LedgerCardDataListExpander, StartAdornmentIcon, useCardConfig, useCardThemeConfig } from '../ledger-card';
import { useLedger } from '@dxs-ts/ledger-api';
import { LedgerCard } from '../ledger-card';
import { DateTime } from 'luxon';
import { Box, Typography } from '@mui/material';


export type FactoryCardId = 'ledger_main' | 'ledger_payments' | 'ledger_money_requests' | 'ledger_black_books';

export const LEDGER_CARD_IDS: FactoryCardId[] = [
  'ledger_main',
  'ledger_payments',
  'ledger_money_requests',
  'ledger_black_books'
];

const defaultExpandedCards: FactoryCardId[] = ['ledger_main', 'ledger_payments', 'ledger_money_requests', 'ledger_black_books'];

export const LedgerCardFactory: React.FC<{ cardId: CardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: FactoryCardId = initProps.cardId as FactoryCardId;
  const { ledgerContainer } = useLedger();
  const { ledger, payments, moneyRequests, blackBooks, blackBookDetails } = ledgerContainer;

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
        </LedgerCard >
      );
    }
    case 'ledger_payments': {
      return (
        <LedgerCard title={intl.formatMessage({ id: 'ledgercard.title.ledgerPayments' })}
          {...commonProps}
          isMenu
          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={true}
          onEdit={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={CreditCardIcon} />}
          onDoubleClick={handleEdit}
        //editDialog={isEditOpen && (<CustomerMessagesEditDialog open onClose={handleEditClose} />)}
        >
          <LedgerCardDataList
            columns={[
              { key: "description", label: intl.formatMessage({ id: 'ledgercard.payment.description' }), width: "40%" },
              { key: "amount", label: intl.formatMessage({ id: 'ledgercard.payment.amount' }), width: "20%" },
              { key: "date", label: intl.formatMessage({ id: 'ledgercard.payment.date' }), width: "20%" },
              { key: "type", label: intl.formatMessage({ id: 'ledgercard.payment.type' }), width: "20%" }
            ]}
            rows={payments.map(p => ({
              description: p.paymentDescription ?? "-",
              amount: p.paymentAmount,
              date: formatAnyDateShort(p.paymentDate),
              type: p.paymentType
            }))}
          />
        </LedgerCard>
      );
    }
    case 'ledger_money_requests': {
      return (
        <LedgerCard title={intl.formatMessage({ id: 'ledgercard.title.ledgerMoneyRequests' })}
          {...commonProps}
          isMenu
          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={true}
          onEdit={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={RequestPageOutlinedIcon} />}
          onDoubleClick={handleEdit}
        //editDialog={isEditOpen && (<CustomerMessagesEditDialog open onClose={handleEditClose} />)}
        >
          <LedgerCardDataList
            columns={[
              { key: "description", label: intl.formatMessage({ id: 'ledgercard.moneyRequest.description' }), width: "40%" },
              { key: "amount", label: intl.formatMessage({ id: 'ledgercard.moneyRequest.amount' }), width: "20%" },
              { key: "date", label: intl.formatMessage({ id: 'ledgercard.moneyRequest.targetDate' }), width: "20%" },
              { key: "type", label: intl.formatMessage({ id: 'ledgercard.moneyRequest.type' }), width: "20%" }
            ]}
            rows={moneyRequests.map(mr => {
              const payment = payments.find(p => p.id === mr.paymentId);
              return {
                description: payment?.paymentDescription ?? "-",
                amount: mr.requestAmount,
                date: formatAnyDateShort(mr.requestTargetDate),
                type: mr.requestType
              };
            })}
          />
        </LedgerCard>
      );
    }
    case 'ledger_black_books': {
      return (
        <LedgerCard title={intl.formatMessage({ id: 'ledgercard.title.ledgerBlackBooks' })}
          {...commonProps}
          isMenu
          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={true}
          onEdit={handleEdit}
          startAdornmentIcon={<StartAdornmentIcon icon={AccountBalanceWalletIcon} />}
          onDoubleClick={handleEdit}
        //editDialog={isEditOpen && (<CustomerMessagesEditDialog open onClose={handleEditClose} />)}
        >
          <LedgerCardDataListExpander
            columns={[
              { key: "description", label: intl.formatMessage({ id: 'ledgercard.blackBook.description' }), width: "40%" },
              { key: "amount", label: intl.formatMessage({ id: 'ledgercard.blackBook.amount' }), width: "20%" },
              { key: "bookDate", label: intl.formatMessage({ id: 'ledgercard.blackBook.bookDate' }), width: "20%" },
              { key: "type", label: intl.formatMessage({ id: 'ledgercard.blackBook.type' }), width: "20%" },
            ]}

            rows={blackBooks
              .map(book => [
                book.bookDescription ?? "-",
                book.bookAmount.toString() ?? "-",
                formatAnyDateShort(book.bookDate),
                book.bookType ?? "-",
              ])}
            expanderContent={blackBooks
              .map(book => (blackBookDetails[book.id] ?? []).flatMap(e => {
                const logs: string[] = e.detailBody?.logs ?? [];
                return logs.map(l => l.split(","));
              }))} />
        </LedgerCard>
      );
    }

    default: return null;
  }
}

function formatAnyDateShort(value: Date | string | undefined): string {
  if (!value) return '--';
  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
}

