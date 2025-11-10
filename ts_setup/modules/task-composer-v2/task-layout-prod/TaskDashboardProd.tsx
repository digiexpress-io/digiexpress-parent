import React from 'react';
import { Grid2, Typography } from '@mui/material';
import { TaskFormReviewDrawer } from '../task-form-review';
import { TaskDashboardContextProvider } from '../task-dashboard';
import { FactoryCardId, TaskCardFactory } from '../task-card-factory';
import {
  CardConfigContextProvider,
  DraggableCardWrapper, taskCardGridSize, TaskCardId, TaskCardStyleDefinition,
  useCardConfig, useDragCardController, useTaskCardThemeConfig
} from '../task-card';
import { useIntl } from 'react-intl';
import { useTaskBackend } from '@dxs-ts/task-api';




const _variant_debug: FactoryCardId[] = [
  'task_main',
  'task_form_summary',
  'assignees_roles',
  'status_priority',
  'customer_messages',
  'feedback',
  'files',
  'notes',
  'task_meta',
  'assignable',
  'transfer',
  'audit_viewers',
  'audit_commits',
  'audit_queues',
  'audit_queue_messages',
  'audit_queue_bindings',
  'audit_queue_deliveries',
  'audit_processes',
  'audit_flow',
];

const _variant_prod: FactoryCardId[] = [
  'task_main_alt',
  'assignable',
  'customer_messages',
  'feedback',
  'files',
  'notes',
  'assignees_roles',
  'status_priority',

];


const _variant_prod_audit: FactoryCardId[] = [
  'audit_viewers',
  'audit_commits',
  'audit_queues',
  'audit_queue_messages',
  'audit_queue_bindings',
  'audit_queue_deliveries',
  'audit_processes',
  'audit_flow',
  'audit_ai'
];



export interface TaskDashboardProdProps {
  cards?: TaskCardId[];
  style?: TaskCardStyleDefinition;
}


export const TaskDashboardProdInternal: React.FC<TaskDashboardProdProps> = (props) => {
  const intl = useIntl();
  const { isReviewOpen, cardTheme, toggleReview, cardOrder } = useCardConfig();

  const { getDragPropsForId, draggingId } = useDragCardController();

  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];


  return (
    <Grid2 container spacing={style.cardSpacing} m={2}>
      <Grid2 container size={{ xs: 12, md: isReviewOpen ? 6 : 12 }} spacing={style.cardSpacing}>
        <Typography variant='h1'>{intl.formatMessage({ id: 'task.edit' })}</Typography>

        {cardOrder.map((cardId) => (
          <Grid2 key={cardId} size={isReviewOpen ? taskCardGridSize.singleCol : taskCardGridSize[cardTheme]}>
            <DraggableCardWrapper {...getDragPropsForId(cardId)} draggingId={draggingId}>
              <TaskCardFactory cardId={cardId} />
            </DraggableCardWrapper>
          </Grid2>
        ))}
      </Grid2>

      <TaskFormReviewDrawer onClose={toggleReview} open={isReviewOpen} />
    </Grid2>)
}



export const TaskDashboardProd: React.FC<{ taskId: string }> = (props) => {
  const { features } = useTaskBackend();

  const initialCardOrder: FactoryCardId[] = React.useMemo(() => {

  return [
    ..._variant_prod,
    ...( features.isAuditTaskEnabled ?_variant_prod_audit : [])
  ];

  }, [features])


  return (
    <TaskDashboardContextProvider taskId={props.taskId}>
      <CardConfigContextProvider cardTheme='large' initialCardOrder={initialCardOrder}>
        <TaskDashboardProdInternal />
      </CardConfigContextProvider>
    </TaskDashboardContextProvider>

  );
}


