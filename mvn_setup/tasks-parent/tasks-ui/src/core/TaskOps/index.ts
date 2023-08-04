import { CreateTaskView } from './CreateTaskView';
import EditTaskDialog  from './EditTask';
import StartTaskDialog  from './StartTask';


const ChangeTaskView = CreateTaskView;
const ChangeTasksView = CreateTaskView;
const ArchiveTaskView = CreateTaskView;



const result = { CreateTaskView, ChangeTaskView, ArchiveTaskView, ChangeTasksView, EditTaskDialog, StartTaskDialog };

export default result;