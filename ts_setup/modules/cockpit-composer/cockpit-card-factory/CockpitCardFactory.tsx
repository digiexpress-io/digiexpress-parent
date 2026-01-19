import React from 'react';
import { Build as BuildIcon } from '@mui/icons-material';
import { Speed as SpeedIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';

import {
  CockpitCard, CockpitCardId, useCockpitCardConfig,
  useCockpitCardThemeConfig, StartAdornmentIcon
} from '../cockpit-card';
import { useCockpit } from '../cockpit-provider';

export type CockpitFactoryCardId = 'cockpit_main' | 'cockpit_wrench_stencil_config';

export const COCKPIT_CARD_IDS: CockpitFactoryCardId[] = [
  'cockpit_main',
  'cockpit_wrench_stencil_config'
];

const defaultExpandedCards: CockpitFactoryCardId[] = ['cockpit_main', 'cockpit_wrench_stencil_config'];

export const CockpitCardFactory: React.FC<{ cardId: CockpitCardId }> = (initProps) => {
  const intl = useIntl();
  const cardId: CockpitFactoryCardId = initProps.cardId as CockpitFactoryCardId;
  const { cockpitContainer } = useCockpit();
  const { config } = cockpitContainer;

  const {
    cardTheme, editingCardId, toggleReview,
    isCardFlashy, toggleCardFlashy, setEditCard,
    isCardExpanded, toggleCardExpanded, expandedCards
  } = useCockpitCardConfig();

  const styleConfig = useCockpitCardThemeConfig();
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
    case 'cockpit_main':
      return (
        <CockpitCard title={intl.formatMessage({ id: 'cockpitcard.cockpitMain.title' }, { cockpitId: config.cockpitConfigName })}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && <div>Edit dialog placeholder</div>}
          startAdornmentIcon={<StartAdornmentIcon icon={SpeedIcon} />}

          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
          <div>
            <p><strong>ID:</strong> {config.id}</p>
            <p><strong>Name:</strong> {config.cockpitConfigName}</p>
            <p><strong>Description:</strong> {config.cockpitConfigDesc}</p>
            <p><strong>Commit ID:</strong> {config.commitId}</p>
            <p><strong>External ID:</strong> {config.externalId || 'N/A'}</p>
          </div>
        </CockpitCard>
      );

    case 'cockpit_wrench_stencil_config':
      return (
        <CockpitCard title={intl.formatMessage({ id: 'cockpitcard.cockpitWrenchStencilConfig.title' })}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && <div>Wrench & Stencil config edit dialog placeholder</div>}
          startAdornmentIcon={<StartAdornmentIcon icon={BuildIcon} />}

          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
          <div>
            <p><strong>Wrench Tools:</strong></p>
            <p>Cockpit wrench tools and configuration options will go here.</p>
            <br />
            <p><strong>Stencil Configuration:</strong></p>
            <p>Cockpit stencil configuration and management tools will go here.</p>
            <p>This card manages both wrench and stencil functionality for the cockpit.</p>
          </div>
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