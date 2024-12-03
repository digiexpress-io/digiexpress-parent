
import React from 'react';

import { Route, Outlet, Routes, useParams } from 'react-router-dom';

import { Composer } from '../stencil/context';
import * as Burger from '@/burger';
import { BurgerApi } from '@/burger';
import { Secondary } from './Secondary';
import { Toolbar } from './Toolbar';

import feedbackIntl from './intl';
import { UpsertOneFeedback } from './upsert-one-feedback';
import { FeedbackAllTasks } from './feedbackAllTasks';



export * from './feedback-api';

const composer: BurgerApi.App<Composer.ContextType> = {
  id: "feedback-composer",
  components: { primary: Outlet, secondary: Secondary, toolbar: Toolbar },
  state: [
    (children: React.ReactNode, restorePoint?: BurgerApi.AppState<Composer.ContextType>) => (<>{children}</>),
    () => ({})
  ]
}

const StartComposer: React.FC<{}> = () => {
  return (<Burger.Provider children={[composer]} drawerOpen />)
}

interface FeedbackComposerProps {

}

export const FeedbackComposer: React.FC<FeedbackComposerProps> = () => {
  const { taskId } = useParams<{ taskId: string }>();
  function handleOnComplete() {

  }


  return (<Routes>
    <Route element={<StartComposer />}>
      <Route path='/feedback/:taskId' element={<UpsertOneFeedback taskId={taskId!} onComplete={handleOnComplete} />} />
      <Route path='/feedback/all-tasks' element={<FeedbackAllTasks />} />
      <Route path='/feedback' element={<>feedback loading...</>} />
    </Route>
  </Routes>)
}


export { feedbackIntl, UpsertOneFeedback };

