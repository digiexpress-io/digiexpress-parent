import TaskSearch from './TaskSearch';

import MyWork from './MyWork';
import Dev from './Dev';
import TeamSpace from './TeamSpace';
import Inbox from './Inbox';
import MyOverview from './MyOverview';

export * from './TaskEdit';
export * from './TaskAssignees';
export * from './TaskDueDate';
export * from './TaskStatus';
export * from './TaskPriority';
export * from './TaskRoles';

const result = { TaskSearch, MyWork, Dev, TeamSpace, Inbox, MyOverview };

export default result;