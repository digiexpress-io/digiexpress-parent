import React from 'react';
import { useIntl } from 'react-intl';


import { EditOutlined as EditOutlinedIcon } from '@mui/icons-material';
import { CreditCard as CreditCardIcon } from '@mui/icons-material';
import { RequestPageOutlined as RequestPageOutlinedIcon } from '@mui/icons-material';

import { useLedger } from '@dxs-ts/ledger-api';

import { LedgerCard, LedgerCardDataList, LedgerCardDataRowText, StartAdornmentIcon } from '../ledger-card';
import { useLedgerCard } from './LedgerCardProvider';
import { LedgerCardBB } from '../ledger-card-bb';
import { formatAnyDateShort } from '@dxs-ts/xui-datetime';



export const LedgerCardFactoryMapping: React.FC<{ }> = (initProps) => {
  const intl = useIntl();
  const { ledgerContainer } = useLedger();
  const commonProps = useLedgerCard();
  const handleEdit = commonProps.onEditOpen;

  const { ledger, payments, moneyRequests } = ledgerContainer;

  switch (commonProps.id) {
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
          <LedgerCardDataRowText label={intl.formatMessage({ id: 'ledgercard.ledgerMain.description' })} value={ledger.description} />
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
      return (<LedgerCardBB />);
    }

    default: return null;
  }
}



