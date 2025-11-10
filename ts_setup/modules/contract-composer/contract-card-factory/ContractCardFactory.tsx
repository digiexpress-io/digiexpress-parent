import React from 'react';
import { Box, Divider, Stack, Typography } from '@mui/material';
import { TaskAlt as TaskAltIcon } from '@mui/icons-material';
import { Handshake as HandshakeIcon } from '@mui/icons-material';

import { AdminPanelSettingsOutlined as AdminPanelSettingsOutlinedIcon } from '@mui/icons-material';
import { EditOutlined as EditOutlinedIcon } from '@mui/icons-material';
import { AttachFileOutlined as AttachFileOutlinedIcon } from '@mui/icons-material';
import { ThumbUpAltOutlined as ThumbUpAltOutlinedIcon } from '@mui/icons-material';
import { PriorityHigh as PriorityHighIcon } from '@mui/icons-material';
import { PersonSearchOutlined as PersonSearchOutlinedIcon } from '@mui/icons-material';
import { AssignmentIndOutlined as AssignmentIndOutlinedIcon } from '@mui/icons-material';
import { SaveOutlined as SaveOutlinedIcon } from '@mui/icons-material';
import { CloudOutlined as CloudOutlinedIcon } from '@mui/icons-material';
import { AccountTreeOutlined as AccountTreeOutlinedIcon } from '@mui/icons-material';
import { DriveFileMoveOutlined as DriveFileMoveOutlinedIcon } from '@mui/icons-material';
import { History as HistoryIcon } from '@mui/icons-material';
import { NoteAltOutlined as NoteAltOutlinedIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';


import { ContractCard, ContractCardDataRowElement, ContractCardId, useCardConfig, ContractCardDataRowText, useContractCardThemeConfig, StartAdornmentIcon } from '../contract-card';
import { useContract } from '@dxs-ts/contract-api';


export type FactoryCardId =
  'contract_main';

export const CONTRACT_CARD_IDS: FactoryCardId[] = [
  'contract_main',

];

const defaultExpandedCards: FactoryCardId[] = ['contract_main'];

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
          startAdornmentIcon={<StartAdornmentIcon icon={HandshakeIcon} />}

          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.category' })} value={contract.contractData?.category} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.issueDate' })} value={contract.contractData?.issueDate} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.productName' })} value={contract.contractData?.productName} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.productCode' })} value={contract.contractData?.productCode} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.limits.annualMaxContribution' })} value={contract.contractData?.limits.annualMaxContribution} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.limits.contractMinValue' })} value={contract.contractData?.limits.contractMinValue} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'contractcard.body.limits.partialWithdrawalMin' })} value={contract.contractData?.limits.partialWithdrawalMin} style={style} />
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




