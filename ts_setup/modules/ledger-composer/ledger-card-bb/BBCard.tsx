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
import { useLedgerCard } from '../ledger-card-factory';
import { formatAnyDateShort } from '@dxs-ts/xui-datetime';



export const LedgerCardBB: React.FC<{}> = () => {
  const intl = useIntl();
  const { ledgerContainer } = useLedger();
  const { blackBooks, blackBookDetails } = ledgerContainer;

  const commonProps = useLedgerCard();


  return (
    <LedgerCard title={intl.formatMessage({ id: 'ledgercard.title.ledgerBlackBooks' })}
      {...commonProps}
      isMenu
      showFlashyToggle={true}
      showEditOnMenu={true}
      showEditButton={true}
      showReviewOnMenu={true}
      onEdit={commonProps.onEditOpen}
      startAdornmentIcon={<StartAdornmentIcon icon={AccountBalanceWalletIcon} />}
      onDoubleClick={commonProps.onEditOpen}
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
          .map(book => ({
            description: book.bookDescription ?? "-",
            amount: book.bookAmount.toString() ?? "-",
            bookDate: formatAnyDateShort(book.bookDate),
            type: book.bookType ?? "-"
          }))}
        expanderContent={blackBooks.map(book => {
          const details = blackBookDetails[book.id] ?? [];

          if (details.length === 0) {
            return <>{intl.formatMessage({ id: 'ledgercard.blackBook.details.none' })}</>
          }

          return (
            <Box key={book.id}>
              {details.map(detail => {
                const logs: string[] = detail.detailBody?.logs ?? [];
                return logs.map((log, i) => (
                  <div key={i}>{log}</div>
                ));
              })}
            </Box>
          );
        })}
      />
    </LedgerCard>
  );
}

