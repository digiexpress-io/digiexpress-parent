import React from 'react';
import { Box, Divider, Stack, Typography } from '@mui/material';


import { HandshakeOutlined as HandshakeOutlinedIcon } from '@mui/icons-material';
import { CalendarMonthOutlined as CalendarMonthOutlinedIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';


import { ContractCard, ContractCardDataRowElement, ContractCardId, useCardConfig, ContractCardDataRowText, useContractCardThemeConfig, StartAdornmentIcon } from '../contract-card';
import { useContract } from '@dxs-ts/contract-api';


export type FactoryCardId = 'contract_main' | 'contract_dates';

export const CONTRACT_CARD_IDS: FactoryCardId[] = [
  'contract_main',
  'contract_dates'
];

const defaultExpandedCards: FactoryCardId[] = ['contract_main', 'contract_dates'];

export const ContractCardFactory: React.FC<{ cardId: ContractCardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: FactoryCardId = initProps.cardId as FactoryCardId;
  const { contractContainer } = useContract();
  const { contract } = contractContainer;

  console.log(contract)


  const {
    cardTheme, editingCardId, toggleReview,
    isCardFlashy, toggleCardFlashy, setEditCard,
    isCardExpanded, toggleCardExpanded, expandedCards
  } = useCardConfig();

  const styleConfig = useContractCardThemeConfig();
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
   
    case 'contract_main':
      return (
        <ContractCard title={intl.formatMessage({ id: 'contractcard.contractMain.title' }, { contractId: contract.contractNumber })}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && (<></>)}
          startAdornmentIcon={<StartAdornmentIcon icon={HandshakeOutlinedIcon} />}

          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.category' })} value={contract.contractData?.category} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.status' })} value={contract.contractStatus} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.productName' })} value={contract.contractData?.productName} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.productCode' })} value={contract.contractData?.productCode} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.limits.annualMaxContribution' })} value={contract.contractData?.limits.annualMaxContribution} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.limits.contractMinValue' })} value={contract.contractData?.limits.contractMinValue} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.limits.partialWithdrawalMin' })} value={contract.contractData?.limits.partialWithdrawalMin} style={style} />
        </ContractCard>
      );

    case 'contract_dates':
      return (<ContractCard title={intl.formatMessage({ id: 'contractcard.contractDates.title' })}
        {...commonProps}
        isMenu
        onDoubleClick={handleEdit}
        onEdit={handleEdit}
        editDialog={editingCardId === cardId && (<></>)}
        startAdornmentIcon={<StartAdornmentIcon icon={CalendarMonthOutlinedIcon} />}

        showFlashyToggle={true}
        showEditOnMenu={true}
        showEditButton={true}
        showReviewOnMenu={false}
      >
        <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.issueDate' })} value={contract.contractData?.issueDate} style={style} />
        <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.issueDateInterval' })} value={contract.contractIssueDateInterval} style={style} />
        <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.issueDateType' })} value={contract.contractIssueDateType} style={style} />
        <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.startDate' })} value={contract.contractStartDate} style={style} />
        <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.startDateInterval' })} value={contract.contractStartDateInterval} style={style} />
        <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.startDateType' })} value={contract.contractStartDateType} style={style} />
        <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.maturityDate' })} value={contract.contractMaturityDate ?? "-"} style={style} />
        <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.maturityDateInterval' })} value={contract.contractMaturityDateInterval ?? "-"} style={style} />
        <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.maturityDateType' })} value={contract.contractMaturityDateType ?? "-"} style={style} />


      </ContractCard>
      );

    default:
      return null;
  }
}

function _formatAnyDateShort(value: Date | string | undefined): string {
  if (!value) return '--';
  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
}




