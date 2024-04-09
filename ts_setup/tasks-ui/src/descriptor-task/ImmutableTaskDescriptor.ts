import { UserProfileAndOrg } from 'descriptor-access-mgmt';
import { Task, TaskExtension } from './backend-types';
import { TaskDescriptor, AssigneeGroupType, TeamGroupType, CustomerId } from './descriptor-types';
import { getDaysUntilDue, getMyWorkType, getTeamspaceType } from './util';



class ImmutableTaskDescriptor implements TaskDescriptor {
  private _entry: Task;
  private _created: Date;
  private _dialobId: string | undefined;
  private _customerId: CustomerId | undefined;
  private _dueDate: Date | undefined;
  private _startDate: Date | undefined;
  private _daysUntilDue: number | undefined;
  private _uploads: TaskExtension[];
  private _myWorkType: AssigneeGroupType | undefined;
  private _teamspaceType: TeamGroupType | undefined;
  private _profile: UserProfileAndOrg;

  constructor(entry: Task, profile: UserProfileAndOrg, today: Date) {
    this._entry = entry;
    this._created = new Date(entry.created);
    this._startDate = entry.startDate ? new Date(entry.startDate) : undefined;
    this._dueDate = entry.dueDate ? new Date(entry.dueDate) : undefined;
    this._daysUntilDue = entry.dueDate ? getDaysUntilDue(entry, today) : undefined;
    this._dialobId = entry.extensions.find(t => t.type === 'dialob')?.body;
    this._customerId = entry.extensions.find(t => t.type === 'CUSTOMER')?.body;
    this._uploads = entry.extensions.filter(t => t.type === 'upload');
    this._myWorkType = getMyWorkType(entry, profile, today);
    this._teamspaceType = getTeamspaceType(entry, profile, today);
    this._profile = profile;
  }
  
  get transactions() { return this._entry.transactions }
  get assigneeGroupType() { return this._myWorkType }
  get teamGroupType() { return this._teamspaceType }
  get profile() { return this._profile }
  get id() { return this._entry.id }
  get dialobId() { return this._dialobId }
  get customerId() { return this._customerId }
  get entry() { return this._entry }
  get created() { return this._created }
  get dueDate() { return this._dueDate }
  get startDate() { return this._startDate }
  get checklist() { return this._entry.checklist }
  get daysUntilDue() { return this._daysUntilDue }

  get comments() { return this._entry.comments }
  get status() { return this._entry.status }
  get priority() { return this._entry.priority }
  get roles() { return this._entry.roles }
  get assignees() { return this._entry.assigneeIds }
  get labels() { return this._entry.labels }
  get title() { return this._entry.title }
  get description() { return this._entry.description }
  get uploads() { return this._uploads }
}

export { ImmutableTaskDescriptor };
