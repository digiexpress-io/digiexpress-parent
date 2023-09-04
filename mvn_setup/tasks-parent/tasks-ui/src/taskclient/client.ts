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
    avatar: "CI"
  },
  {
    displayName: "Sam Vimes",
    userId: "sam vimes",
    userRoles: ["admin-role"],
    avatar: "SV"
  },
  {
    displayName: "Lord Vetinari",
    userId: "lord vetinari",
    userRoles: ["admin-role"],
    avatar: "LV"
  },
  {
    displayName: "Lady Sybil Vimes",
    userId: "lady sybil vimes",
    userRoles: ["admin-role"],
    avatar: "LS"
  },
  {
    displayName: "Nobby Nobbs",
    userId: "nobby nobbs",
    userRoles: ["admin-role"],
    avatar: "NN"
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