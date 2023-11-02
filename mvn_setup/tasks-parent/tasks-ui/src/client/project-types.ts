type RepoType = 'wrench' | 'stencil' | 'tasks' | 'dialob';
export type ProjectId = string;

export interface Project {
  id: string;
  repoId: string;
  repoType: RepoType;
  title: string;
  description: string;
  users: string[];
  created: string;
  updated: string;
}

export interface ProjectTransaction {
  id: string;
  commands: ProjectCommand[];
}

export interface ProjectCommand {
  userId?: string;
  targetDate?: string;
  commandType: ProjectCommandType;
}

export type ProjectCommandType = 'ArchiveProject' | 'CreateProject' | 'AssignProjectUsers' | 'ChangeProjectInfo';


export interface ProjectUpdateCommand<T extends ProjectCommandType> extends ProjectCommand {
  projectId: ProjectId;
  commandType: T;
}

export interface CreateProject extends ProjectCommand {
  commandType: 'CreateProject';
  repoId: string;
  repoType: RepoType;
  title: string;
  description: string
  users: string[];
}

export interface ArchiveProject extends ProjectUpdateCommand<'ArchiveProject'> {
}

export interface AssignProjectUsers extends ProjectUpdateCommand<'AssignProjectUsers'> {
  users: string[];
}

export interface ChangeProjectInfo extends ProjectUpdateCommand<'ChangeProjectInfo'> {
  title: string;
  description: string;
}

export interface ProjectPagination {
  page: number; //starts from 1
  total: { pages: number, records: number };
  records: Project[];
}

export interface ProjectStore {
  getActiveProjects(): Promise<ProjectPagination>
  getActiveProject(id: string): Promise<Project>
  createProject(command: CreateProject): Promise<Project>
  updateActiveProject(id: string, commands: ProjectUpdateCommand<any>[]): Promise<Project>
}


export const mockProjects: Record<RepoType, Project> = {
  wrench: {
    id: 'wrench-1',
    repoId: 'repo-1',
    repoType: 'wrench',
    title: 'wrench-dev',
    description: 'Wrench assets for dev',
    users: ['sipoo-user'],
    created: "2023-11-02T06:36:45.959Z",
    updated: "2023-11-03T06:36:45.959Z",
  },
  dialob: {
    id: 'dialob-1',
    repoId: 'repo-1',
    repoType: 'dialob',
    title: 'dialob-dev',
    description: 'Dialob assets for dev',
    users: ['sipoo-user'],
    created: "2023-11-02T06:36:45.959Z",
    updated: "2023-11-03T06:36:45.959Z",
  },
  stencil: {
    id: 'stencil-1',
    repoId: 'repo-1',
    repoType: 'stencil',
    title: 'stencil-dev',
    description: 'stencil assets for dev',
    users: ['sipoo-user'],
    created: "2023-11-02T06:36:45.959Z",
    updated: "2023-11-03T06:36:45.959Z",
  },
  tasks: {
    id: 'tasks-1',
    repoId: 'repo-1',
    repoType: 'tasks',
    title: 'tasks-dev',
    description: 'Task assets for dev',
    users: ['sipoo-user'],
    created: "2023-11-02T06:36:45.959Z",
    updated: "2023-11-03T06:36:45.959Z",
  }
}
