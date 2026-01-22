import React from 'react';
import { Build as BuildIcon } from '@mui/icons-material';
import { Speed as SpeedIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';

import {
  CockpitCard, CockpitCardId, useCockpitCardConfig,
  StartAdornmentIcon, CockpitCardDataRowTextWithDescription, CockpitCardDataRowText,
  CockpitCardDataRowElement
} from '../cockpit-card';
import { useCockpit } from '@dxs-ts/cockpit-api';
import { CockpitTenantConfigureDialog } from '../cockpit-tenant-configure';
import { CockpitEditDialog } from '../cockpit-edit';
import { CockpitStatusIndicator } from '../cockpit-status-indicator';

export type CockpitFactoryCardId = 'cockpit_main' | 'cockpit_wrench_stencil_config';

export const COCKPIT_CARD_IDS: CockpitFactoryCardId[] = [
  'cockpit_main',
  'cockpit_wrench_stencil_config'
];

const defaultExpandedCards: CockpitFactoryCardId[] = ['cockpit_main', 'cockpit_wrench_stencil_config'];

export const CockpitCardFactory: React.FC<{ cardId: CockpitCardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: CockpitFactoryCardId = initProps.cardId as CockpitFactoryCardId;
  const { cockpitContainer, tenants, activity } = useCockpit();
  const { config } = cockpitContainer;
  const isActiveCockpit = activity.activeCockpitId === cockpitContainer.config.id;

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
        <CockpitCard title={intl.formatMessage({ id: 'cockpitcard.cockpitMain.title' }, { cockpitId: config.cockpitConfigName })}
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
          <CockpitCardDataRowText label={intl.formatMessage({ id: 'cockpit.name' })} value={config.cockpitConfigName} />
          <CockpitCardDataRowText label={intl.formatMessage({ id: 'cockpit.description' })} value={config.cockpitConfigDesc} />
          <CockpitCardDataRowElement label={intl.formatMessage({ id: 'cockpit.status' })} value={isActiveCockpit ? <CockpitStatusIndicator isActive={true} /> : <CockpitStatusIndicator isActive={false} />} />

        </CockpitCard>
      );

    case 'cockpit_wrench_stencil_config':
      return (
        <CockpitCard title={intl.formatMessage({ id: 'cockpitcard.cockpitWrenchStencilConfig.title' })}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && (<CockpitTenantConfigureDialog open={isEditOpen} onClose={handleEditClose} cockpitId={config.id} />)}
          startAdornmentIcon={<StartAdornmentIcon icon={BuildIcon} />}

          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
          <CockpitCardDataRowTextWithDescription
            label={intl.formatMessage({ id: 'cockpitcard.wrenchStencil.wrenchConfig.title' })}
            value={tenants.wrench?.externalId ?? '-'}
            description={tenants.wrench?.cockpitConfigTenantDesc}
          />
          <CockpitCardDataRowTextWithDescription
            label={intl.formatMessage({ id: 'cockpitcard.wrenchStencil.stencilConfig.title' })}
            value={tenants.stencil?.externalId ?? '-'}
            description={tenants.stencil?.cockpitConfigTenantDesc}
          />

        </CockpitCard>
      );

    default:
      return null;
  }
}

function formatAnyDateShort(value: Date | string | undefined): string {
  if (!value) return '--';
  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
}