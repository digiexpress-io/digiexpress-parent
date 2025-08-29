import React from 'react';
import { Grid2, Typography } from '@mui/material';
import { TaskFormReviewDrawer } from '../task-form-review';
import { TaskDashboardContextProvider } from '../task-dashboard';
import { TaskCardFactory } from '../task-card-factory';
import {
  CardConfigContextProvider,
  DraggableCardWrapper, taskCardGridSize, TaskCardId, TaskCardStyleDefinition,
  useCardConfig, useDragCardController, useTaskCardThemeConfig
} from '../task-card';
import { useIntl } from 'react-intl';




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
  'customer_messages',
  'feedback',
  'files',
  'notes',
  'assignees_roles',
  'status_priority',
];



export interface TaskDashboardProdProps {
  cards?: TaskCardId[];
  style?: TaskCardStyleDefinition;
}


export const TaskDashboardProdInternal: React.FC<TaskDashboardProdProps> = (props) => {
  const intl = useIntl();
  const { isReviewOpen, cardTheme, toggleReview, cardOrder } = useCardConfig();

  const { getDragPropsForId } = useDragCardController();

  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];
  
  return (
    <Grid2 container spacing={style.cardSpacing} m={2}>
      <Grid2 container size={{ xs: 12, md: isReviewOpen ? 6 : 12 }} spacing={style.cardSpacing}>
        <Typography variant='h1'>{intl.formatMessage({ id: 'task.edit' })}</Typography>

        {cardOrder.map((cardId) => (
          <Grid2 key={cardId} size={isReviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[cardTheme]}>
            <DraggableCardWrapper {...getDragPropsForId(cardId)}>
              <TaskCardFactory cardId={cardId} />
            </DraggableCardWrapper>
          </Grid2>
        ))}
      </Grid2>

      <TaskFormReviewDrawer onClose={toggleReview} open={isReviewOpen} />
    </Grid2>)
}



export const TaskDashboardProd: React.FC<{ taskId: string }> = (props) => {

  return (
    <TaskDashboardContextProvider taskId={props.taskId}>
      <CardConfigContextProvider cardTheme='large' initialCardOrder={_variant_prod}>
        <TaskDashboardProdInternal />
      </CardConfigContextProvider>
    </TaskDashboardContextProvider>

  );
}


