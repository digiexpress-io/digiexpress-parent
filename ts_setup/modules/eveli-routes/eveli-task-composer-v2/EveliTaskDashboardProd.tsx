import React from 'react';
import { Grid2 } from '@mui/material';
import { FormReviewDrawer } from './FormReviewDrawer';
import { EveliTaskDashboardContextProvider } from './EveliTaskDashboardContext';
import { CardFactory } from './EveliTaskDashboard';
import {
  CardConfigContextProvider,
  DraggableCardWrapper, taskCardGridSize, TaskCardId, TaskCardStyleDefinition,
  useCardConfig, useDragCardController, useTaskCardThemeConfig
} from '../eveli-task-composer-v2-task-card';




const _variant_debug: TaskCardId[] = [
  'task_main',
  'task_form_summary',
  'assignees_roles',
  'status_priority',
  'customer_messages',
  'feedback',
  'files',
  'notes',
  'task_meta'
];

const _variant_prod: TaskCardId[] = [
  'task_main_alt',
  'assignees_roles',
  'status_priority',
  'customer_messages',
  'feedback',
  'files',
  'notes',
];



export interface EveliTaskDashboardProdProps {
  cards?: TaskCardId[];
  style?: TaskCardStyleDefinition;
}


const EveliTaskDashboardProdInternal: React.FC<EveliTaskDashboardProdProps> = (props) => {
  const cards = props.cards ?? _variant_prod;
  const { isReviewOpen, cardTheme, toggleReview } = useCardConfig();
  const { getDragPropsForId } = useDragCardController();

  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];
  
  return (
    <Grid2 container spacing={style.cardSpacing} m={1}>
      

      <Grid2 container size={{ xs: 12, md: isReviewOpen ? 6 : 12 }} spacing={style.cardSpacing}
        sx={{ overflowY: 'auto', maxHeight: '100%', overflow: 'visible' }}>
        {cards.map((cardId) => (
          <Grid2 key={cardId} size={isReviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[cardTheme]}>
            <DraggableCardWrapper {...getDragPropsForId(cardId)}>
              <CardFactory cardId={cardId} />
            </DraggableCardWrapper>
          </Grid2>
        ))}
      </Grid2>

      <FormReviewDrawer onClose={toggleReview} open={isReviewOpen} />
    </Grid2>)
}



export const EveliTaskDashboardProd: React.FC<{ taskId: string }> = (props) => {

  return (
    <EveliTaskDashboardContextProvider taskId={props.taskId}>
      <CardConfigContextProvider cardTheme='large'>
        <EveliTaskDashboardProdInternal />
      </CardConfigContextProvider>
    </EveliTaskDashboardContextProvider>

  );
}


