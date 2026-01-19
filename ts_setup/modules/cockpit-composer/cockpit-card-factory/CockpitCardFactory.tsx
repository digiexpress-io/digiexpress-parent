import React from 'react';
import { Typography, Box } from '@mui/material';
import { Build as BuildIcon } from '@mui/icons-material';
import { Speed as SpeedIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';
import { DateTime } from 'luxon';

import {
  CockpitCard, CockpitCardId, useCockpitCardConfig,
  useCockpitCardThemeConfig, StartAdornmentIcon, CockpitCardDataRowText
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
          editDialog={editingCardId === cardId && <Typography>Edit dialog placeholder</Typography>}
          startAdornmentIcon={<StartAdornmentIcon icon={SpeedIcon} />}

          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
          <CockpitCardDataRowText label={intl.formatMessage({ id: 'cockpitcard.body.name' })} value={config.cockpitConfigName} style={style} />
          <CockpitCardDataRowText label={intl.formatMessage({ id: 'cockpitcard.body.description' })} value={config.cockpitConfigDesc} style={style} />
        </CockpitCard>
      );

    case 'cockpit_wrench_stencil_config':
      return (
        <CockpitCard title={intl.formatMessage({ id: 'cockpitcard.cockpitWrenchStencilConfig.title' })}
          {...commonProps}
          isMenu
          onDoubleClick={handleEdit}
          onEdit={handleEdit}
          editDialog={editingCardId === cardId && <Typography>Wrench & Stencil config edit dialog placeholder</Typography>}
          startAdornmentIcon={<StartAdornmentIcon icon={BuildIcon} />}

          showFlashyToggle={true}
          showEditOnMenu={true}
          showEditButton={true}
          showReviewOnMenu={false}
        >
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1 }}>
              {intl.formatMessage({ id: 'cockpitcard.wrenchStencil.wrenchTools.title' })}
            </Typography>
            <Typography variant="body2" sx={{ mb: 2 }}>
              {intl.formatMessage({ id: 'cockpitcard.wrenchStencil.wrenchTools.description' })}
            </Typography>

            <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1 }}>
              {intl.formatMessage({ id: 'cockpitcard.wrenchStencil.stencilConfig.title' })}
            </Typography>
            <Typography variant="body2" sx={{ mb: 1 }}>
              {intl.formatMessage({ id: 'cockpitcard.wrenchStencil.stencilConfig.description' })}
            </Typography>
            <Typography variant="body2">
              {intl.formatMessage({ id: 'cockpitcard.wrenchStencil.combinedDescription' })}
            </Typography>
          </Box>
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