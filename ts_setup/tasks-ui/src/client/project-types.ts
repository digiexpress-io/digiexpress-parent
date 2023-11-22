import { RepoConfigType, RepoType } from "./tenant-config-types";
export type ProjectId = string;

export interface Project {
  id: string;
  name: string;
  created: string;
  updated: string;
  status: string;
  preferences: {
    landingApp: "";
  }
  repoConfigs: {
    repoId: string;
    repoType: RepoConfigType;
  }[]
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

export type ProjectCommandType = 'ArchiveProject' | 'CreateTenantConfig' | 'AssignProjectUsers' | 'ChangeProjectInfo' | 'ChangeRepoType';


export interface ProjectUpdateCommand<T extends ProjectCommandType> extends ProjectCommand {
  projectId: ProjectId;
  commandType: T;
}

export interface CreateProject extends ProjectCommand {
  commandType: 'CreateTenantConfig';
  repoId: string;
  name: string;
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
export interface ChangeRepoType extends ProjectUpdateCommand<'ChangeRepoType'> {
  repoType: RepoType
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
