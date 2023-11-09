import { Profile, Project, RepoType } from 'client';

export interface AvatarCode {
  twoletters: string;
  value: string;
}

export interface ProjectDescriptor {
  entry: Project;
  id: string;

  appId: string;
  repoId: string;
  repoType: RepoType;
  title: string;
  description: string;
  users: string[];
  created: Date;
  updated: Date;

  userAvatars: AvatarCode[];
  profile: Profile;
}


export interface ProjectPaletteType {
  users: Record<string, string>;
  repoType: Record<string, string>;
}
export interface PaletteType {
  repoType: {
    'WRENCH': string,
    'STENCIL': string,
    'TASKS': string,
    'DIALOB': string
  },
  colors: { red: string, green: string, yellow: string, blue: string, violet: string }
}

export type FilterByRepoType = { type: 'FilterByRepoType', repoType: RepoType[], disabled: boolean }
export type FilterByUsers = { type: 'FilterByUsers', users: string[], disabled: boolean }
export type FilterBy = FilterByRepoType | FilterByUsers;

export type GroupBy = 'users' | 'repoType' | 'none';

export interface Group {
  id: string;
  type: GroupBy;
  color?: string;
  records: ProjectDescriptor[];
}

export interface Data {
  projects: ProjectDescriptor[];
  projectsByUser: Record<string, ProjectDescriptor[]>;
  palette: ProjectPaletteType;
  profile: Profile;
  users: string[];
}

export interface GroupsAndFilters {
  groupBy: GroupBy;
  groups: Group[];
  filterBy: FilterBy[];
  searchString: string | undefined;

  withSearchString(searchString: string): GroupsAndFilters;
  withProjects(projects: Data): GroupsAndFilters;
  withGroupBy(groupBy: GroupBy): GroupsAndFilters;
  withFilterByRepoType(repoType: RepoType[]): GroupsAndFilters;
  withFilterByUsers(users: string[]): GroupsAndFilters;
}

export interface ProjectsContextType {
  setState: ProjectsDispatch;
  reload: () => Promise<void>;
  loading: boolean;
  state: ProjectsState,
  palette: PaletteType;
}

export type ProjectsMutator = (prev: ProjectsState) => ProjectsState;
export type ProjectsDispatch = (mutator: ProjectsMutator) => void;

export interface ProjectsState {
  projects: ProjectDescriptor[];
  projectsByUser: Record<string, ProjectDescriptor[]>;
  palette: ProjectPaletteType;
  profile: Profile;
  users: string[];

  withProfile(profile: Profile): ProjectsState;
  withProjects(projects: Project[]): ProjectsState;
  toGroupsAndFilters(): GroupsAndFilters;
}