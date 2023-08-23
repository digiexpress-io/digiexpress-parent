import { CreateTaskView } from './CreateTaskView';
import EditTaskDialog  from './EditTask';
import WorkOnTaskDialog  from './WorkOnTask';


const ChangeTaskView = CreateTaskView;
const ChangeTasksView = CreateTaskView;
const ArchiveTaskView = CreateTaskView;



const result = { CreateTaskView, ChangeTaskView, ArchiveTaskView, ChangeTasksView, EditTaskDialog, WorkOnTaskDialog };

export default result;