import { Client, Store, Org, User, Role } from './client-types';
import type { TaskId, Task, TaskPagination, TaskStore } from './task-types';
import type { Profile, ProfileStore } from './profile-types';
import { } from './client-store';

const mockRoles: Record<string, Role> = {
  "admin-role": {
    roleId: "admin-role",
    avatar: "AR",
    displayName: "Admin Role"
  },
  "water-department": {
    roleId: "water-department",
    avatar: "WD",
    displayName: "Water Department"
  },
  "education-department": {
    roleId: "education-department",
    avatar: "ED",
    displayName: "Education Department"
  },
  "elderly-care-department": {
    roleId: "elderly-care-department",
    avatar: "EC",
    displayName: "Elderly Care Department"
  },
  "sanitization-department": {
    roleId: "sanitization-department",
    avatar: "SD",
    displayName: "Sanitization Department"
  }
};

const mockUsers: User[] = [
  {
    displayName: "Carrot Ironfoundersson",
    userId: "carrot ironfoundersson",
    userRoles: ["admin-role"],
    avatar: "CI",
    activity: [
      {
        id: "activity1",
        eventDate: "2022/04/15",
        eventType: 'task-completed',
        subjectTitle: "Sewage water disposal"
      },
      {
        id: "activity2",
        eventDate: "2023/11/07",
        eventType: 'task-blocked',
        subjectTitle: "Request for elderly care"
      },
      {
        id: "activity3",
        eventDate: "2022/03/04",
        eventType: 'checklist-self-assigned',
        subjectTitle: "checklist1"
      },
      {
        id: "activity4",
        eventDate: "2022/12/09",
        eventType: 'task-completed',
        subjectTitle: "Sewage water disposal"
      },
      {
        id: "activity5",
        eventDate: "2023/11/30",
        eventType: 'task-blocked',
        subjectTitle: "Request for elderly care"
      },
      {
        id: "activity6",
        eventDate: "2023/10/31",
        eventType: 'message-sent',
        subjectTitle: "Building permit"
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
        id: "activity1",
        eventDate: "2023/10/31",
        eventType: 'checklist-completed',
        subjectTitle: "checklist2"
      },
      {
        id: "activity2",
        eventDate: "2022/12/09",
        eventType: 'task-blocked',
        subjectTitle: "General message"
      },
      {
        id: "activity3",
        eventDate: "2023/11/07",
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
        id: "activity1",
        eventDate: "2023/10/31",
        eventType: 'checklist-completed',
        subjectTitle: "checklist2"
      },
      {
        id: "activity2",
        eventDate: "2022/12/09",
        eventType: 'task-blocked',
        subjectTitle: "General message"
      },
      {
        id: "activity3",
        eventDate: "2023/11/07",
        eventType: 'comment-finished',
        subjectTitle: "comment1"
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
        id: "activity1",
        eventDate: "2023/10/31",
        eventType: 'checklist-completed',
        subjectTitle: "checklist2"
      },
      {
        id: "activity2",
        eventDate: "2022/12/09",
        eventType: 'task-blocked',
        subjectTitle: "General message"
      },
      {
        id: "activity3",
        eventDate: "2023/11/07",
        eventType: 'comment-finished',
        subjectTitle: "comment1"
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
        id: "activity1",
        eventDate: "2023/10/31",
        eventType: 'checklist-completed',
        subjectTitle: "checklist2"
      },
      {
        id: "activity2",
        eventDate: "2022/12/09",
        eventType: 'task-blocked',
        subjectTitle: "General message"
      },
      {
        id: "activity3",
        eventDate: "2023/11/07",
        eventType: 'comment-finished',
        subjectTitle: "comment1"
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