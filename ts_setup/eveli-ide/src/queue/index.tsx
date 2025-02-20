
import React from 'react';

import { Route, Outlet, Routes, useParams } from 'react-router-dom';

import { Composer } from '../stencil/context';
import * as Burger from '@/burger';
import { BurgerApi } from '@/burger';
import { Secondary } from './Secondary';
import { Toolbar } from './Toolbar';

import queueIntl from './intl';
import { FindAllQueues } from './find-all-queues';
import { FindAllMessages } from './find-all-messages';
import { FindAllDeliveries } from './find-all-deliveries';

const composer: BurgerApi.App<Composer.ContextType> = {
  id: "queue-composer",
  components: { primary: Outlet, secondary: Secondary, toolbar: Toolbar },
  state: [
    (children: React.ReactNode, restorePoint?: BurgerApi.AppState<Composer.ContextType>) => (<>{children}</>),
    () => ({})
  ]
}

const StartComposer: React.FC<{}> = () => {
  return (<Burger.Provider children={[composer]} />)
}

interface QueueComposerProps {

}

export const QueueComposer: React.FC<QueueComposerProps> = () => {
  return (
    <Routes>
      <Route element={<StartComposer />}>
        <Route path='/queues' element={<FindAllQueues />} />
        <Route path='/queues/messages' element={<FindAllMessages />} />
        <Route path='/queues/deliveries' element={<FindAllDeliveries />} />
      </Route>
    </Routes>)
}

export * from './queue-api';
export { queueIntl };

