import React from 'react';
import { Box, Divider, Stack, Typography } from '@mui/material';
import { TaskAlt as TaskAltIcon } from '@mui/icons-material';
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


export type FactoryCardId =
  'contract_main';

export const CONTRACT_CARD_IDS: FactoryCardId[] = [
  'contract_main',

];

const defaultExpandedCards: FactoryCardId[] = ['contract_main'];

export const ContractCardFactory: React.FC<{ cardId: ContractCardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: FactoryCardId = initProps.cardId as FactoryCardId;
  //const { task } = useTaskDashboard();
  
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
        <ContractCard title={intl.formatMessage({ id: 'contractcard.title.contractId'})}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && (<></>)}
          startAdornmentIcon={<StartAdornmentIcon icon={TaskAltIcon} />}

          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
   
          <ContractCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.customerName', defaultMessage: 'Customer name' })} value={'name'} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.subject', defaultMessage: 'Subject' })} value={'something'} style={style} />
          <ContractCardDataRowText label={intl.formatMessage({ id: 'taskcard.body.additionalInfo', defaultMessage: 'Extra info' })} value={'more'} style={style} />
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




