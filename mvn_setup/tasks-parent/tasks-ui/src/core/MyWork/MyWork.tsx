import React from 'react';
import { MyWorkTasks } from './MyWorkTasks';

import client from '@taskclient';
import Styles from '@styles';


const MyWork: React.FC<{}> = () => {

  return (
    <client.TableProvider>
      <Styles.Layout>
        <MyWorkTasks />
      </Styles.Layout>
    </client.TableProvider>);
}

export { MyWork };
