import React from 'react';
import { Grid2, Typography } from '@mui/material';

import { TaskFormReviewDrawer } from '../task-form-review';
import { TaskDashboardContextProvider, useTaskDashboard } from './TaskDashboardContext';

import {
  DraggableCardWrapper, useDragCardController,
  CardConfigContextProvider,
  useCardConfig, useTaskCardThemeConfig,
  taskCardGridSize,
  TaskCardStyleSelect
} from '../task-card';
import { TaskCardFactory, TASK_CARD_IDS } from '../task-card-factory';
import { useIntl } from 'react-intl';


const TaskDashboardInternal: React.FC = () => {
  const intl = useIntl();
  const { cardOrder, isReviewOpen, cardTheme, setCardTheme, toggleReview } = useCardConfig();
  const { getDragPropsForId } = useDragCardController();

  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];
  const { task } = useTaskDashboard();

  return (
    <Grid2 container spacing={style.cardSpacing} m={1}>
      <Grid2 size={isReviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[cardTheme]}>
        <Typography variant='h1'>{intl.formatMessage({ id: 'task.composer.task.edit', defaultMessage: 'Edit task: ' })} {task.taskRef}</Typography>
        <TaskCardStyleSelect value={cardTheme} onChange={setCardTheme} />
      </Grid2>

      <Grid2 container
        size={{ xs: 12, md: isReviewOpen ? 6 : 12 }}
        spacing={style.cardSpacing}
        sx={{ overflowY: 'auto', maxHeight: '100%', overflow: 'visible' }}>
        {cardOrder.map((cardId) => (
          <Grid2 key={cardId} size={isReviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[cardTheme]}>
            <DraggableCardWrapper {...getDragPropsForId(cardId)}>
              <TaskCardFactory cardId={cardId} />
            </DraggableCardWrapper>
          </Grid2>
        ))}
      </Grid2>

      <TaskFormReviewDrawer onClose={toggleReview} open={isReviewOpen} />
    </Grid2>
  );
};


export const TaskDashboard: React.FC<{ taskId: string }> = (props) => {
  return (
    <TaskDashboardContextProvider taskId={props.taskId}>
      <CardConfigContextProvider initialCardOrder={TASK_CARD_IDS}>
        <TaskDashboardInternal />
      </CardConfigContextProvider>
    </TaskDashboardContextProvider>

  );
}
