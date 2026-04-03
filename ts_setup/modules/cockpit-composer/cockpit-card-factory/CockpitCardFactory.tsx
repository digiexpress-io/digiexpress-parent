import React from 'react';
import { Speed as SpeedIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import {
  CockpitCard, CockpitCardId, useCockpitCardConfig,
  StartAdornmentIcon, CockpitCardDataRowText,
  CockpitCardDataRowElement
} from '../cockpit-card';
import { useCockpit } from '@dxs-ts/cockpit-api';
import { CockpitEditDialog } from '../cockpit-edit';
import { CockpitStatusIndicator } from '../cockpit-status-indicator';



export type CockpitFactoryCardId = 'cockpit_main';
export const COCKPIT_CARD_IDS: CockpitFactoryCardId[] = ['cockpit_main'];


const defaultExpandedCards: CockpitFactoryCardId[] = ['cockpit_main'];

export const CockpitCardFactory: React.FC<{ cardId: CockpitCardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: CockpitFactoryCardId = initProps.cardId as CockpitFactoryCardId;
  const { cockpitContainer } = useCockpit();

  const isActiveCockpit = !!cockpitContainer.member?.aliasStatus;

  const {
    editingCardId, toggleReview, setEditCard,
    isCardExpanded, toggleCardExpanded, expandedCards
  } = useCockpitCardConfig();


  const commonProps = {
    id: cardId,
    isExpanded: expandedCards.find(target => target.cardId === cardId) ? isCardExpanded(cardId) : defaultExpandedCards.includes(cardId),
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
    case 'cockpit_main':
      return (
        <CockpitCard title={intl.formatMessage({ id: 'cockpitcard.cockpitMain.title' }, { cockpitId: cockpitContainer.alias.aliasName })}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && <CockpitEditDialog open={isEditOpen} onClose={handleEditClose} />}
          startAdornmentIcon={<StartAdornmentIcon icon={SpeedIcon} />}

          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
          <CockpitCardDataRowText label={intl.formatMessage({ id: 'cockpit.name' })} value={cockpitContainer.alias.aliasName} />
          <CockpitCardDataRowText label={intl.formatMessage({ id: 'cockpit.description' })} value={cockpitContainer.alias.aliasDesc} />
          <CockpitCardDataRowElement label={intl.formatMessage({ id: 'cockpit.status' })} value={isActiveCockpit ? <CockpitStatusIndicator isActive={true} /> : <CockpitStatusIndicator isActive={false} />} />

        </CockpitCard>
      );
    default:
      return null;
  }
}