import { Client, Store, Org, User } from './client-types';
import type { TaskId, Task, TaskPagination, TaskStore } from './task-types';
import type { Profile, ProfileStore } from './profile-types';
import { } from './client-store';


const mockRoles: string[] = [
  "admin-role",
  "water-department",
  "education-department",
  "elderly-care-department",
  "sanitization-department"
];

const mockUsers: User[] = [
  {
    displayName: "Carrot Ironfoundersson",
    userId: "carrot ironfoundersson",
    userRoles: ["admin-role"],
    avatar: "CI",
    activity: [
      {
        id: "activity1",
        eventDate: "2023-10-09",
        eventType: 'task-completed',
        subjectTitle: "Sewage water disposal"
      },
      {
        id: "activity2",
        eventDate: "2023-09-09",
        eventType: 'task-blocked',
        subjectTitle: "Request for elderly care"
      },
      {
        id: "activity3",
        eventDate: "2023-04-09",
        eventType: 'checklist-self-assigned',
        subjectTitle: "checklist1"
      }
    ]
  },
  {
    displayName: "Sam Vimes",
    userId: "sam vimes",
    userRoles: ["admin-role"],
    avatar: "SV",
    activity: [
      {
        id: "activity4",
        eventDate: "2023-09-09",
        eventType: 'checklist-completed',
        subjectTitle: "checklist2"
      },
      {
        id: "activity5",
        eventDate: "2023-11-09",
        eventType: 'task-blocked',
        subjectTitle: "General message"
      },
      {
        id: "activity6",
        eventDate: "2023-05-09",
        eventType: 'comment-finished',
        subjectTitle: "comment1"
      }
    ]
  },
  {
    displayName: "Lord Vetinari",
    userId: "lord vetinari",
    userRoles: ["admin-role"],
    avatar: "LV",
    activity: [
      {
        id: "activity7",
        eventDate: "2023-01-09",
        eventType: 'attachment-added',
        subjectTitle: "attachment1 for task: Sewage water disposal"
      },
      {
        id: "activity8",
        eventDate: "2023-11-09",
        eventType: 'message-sent',
        subjectTitle: "message1"
      },
      {
        id: "activity9",
        eventDate: "2023-02-09",
        eventType: 'comment-finished',
        subjectTitle: "comment2"
      }
    ]
  },
  {
    displayName: "Lady Sybil Vimes",
    userId: "lady sybil vimes",
    userRoles: ["admin-role"],
    avatar: "LS",
    activity: [
      {
        id: "activity10",
        eventDate: "2023-11-08",
        eventType: 'comment-finished',
        subjectTitle: "comment3"
      },
      {
        id: "activity11",
        eventDate: "2023-18-08",
        eventType: 'task-completed',
        subjectTitle: "General message"
      },
      {
        id: "activity12",
        eventDate: "2023-05-09",
        eventType: 'checklist-completed',
        subjectTitle: "checklist3"
      }
    ]
  },
  {
    displayName: "Nobby Nobbs",
    userId: "nobby nobbs",
    userRoles: ["admin-role"],
    avatar: "NN",
    activity: [
      {
        id: "activity13",
        eventDate: "2023-28-08",
        eventType: 'checklist-self-assigned' ,
        subjectTitle: "checklist4"
      },
      {
        id: "activity14",
        eventDate: "2023-10-09",
        eventType: 'task-completed',
        subjectTitle: "Request for elderly care"
      },
      {
        id: "activity15",
        eventDate: "2023-11-09",
        eventType: 'checklist-completed',
        subjectTitle: "checklist5"
      }
    ]
  },
]

const mockOrg: {
  org: Org,
  user: User,
  today: Date
} = {
  org: {
    roles: mockRoles,
    users: mockUsers.reduce((acc, item) => ({ ...acc, [item.userId]: item }), {})
  },
  user: mockUsers[0],
  today: new Date(),
}


type BackendInit = { created: boolean } | null


export class ServiceImpl implements Client {
  private _store: Store;

  constructor(store: Store) {
    this._store = store;
  }

  get config() { return this._store.config; }

  get profile(): ProfileStore {
    return {
      getProfile: () => this.getProfile(),
      createProfile: () => this.createProfile()
    }
  }
  async getProfile(): Promise<Profile> {
    const { today, user } = mockOrg;
    const { userId, userRoles: roles } = user;
    try {
      const init = await this._store.fetch<BackendInit>("init", { notFound: () => null });
      if (init === null) {
        return { name: "", contentType: "BACKEND_NOT_FOUND", today, userId, roles };
      }

      return { name: "", contentType: "OK", today, userId, roles };
    } catch (error) {
      console.error("PROFILE, failed to fetch", error);
      return { name: "", contentType: "NO_CONNECTION", today, userId, roles };
    }
  }

  createProfile(): Promise<Profile> {
    return this._store.fetch<Profile>("head", { method: "POST", body: JSON.stringify({}) });
  }

  get task(): TaskStore {
    return {
      getActiveTasks: () => this.getActiveTasks(),
      getActiveTask: (id: TaskId) => this.getActiveTask(id)
    };
  }

  async getActiveTasks(): Promise<TaskPagination> {
    const tasks = await this._store.fetch<object[]>(`active/tasks`);
    return {
      page: 1,
      total: { pages: 1, records: tasks.length },
      records: tasks as any
    }
  }
  getActiveTask(id: TaskId): Promise<Task> {
    return this._store.fetch<Task>(`tasks/${id}`);
  }
  async org(): Promise<{ org: Org, user: User }> {
    return mockOrg;
  }
}