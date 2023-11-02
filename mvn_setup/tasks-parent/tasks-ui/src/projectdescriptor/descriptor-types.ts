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
    'wrench': string,
    'stencil': string,
    'tasks': string,
    'dialob': string
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

export interface DescriptorState {
  groupBy: GroupBy;
  groups: Group[];
  filterBy: FilterBy[];
  searchString: string | undefined;

  withSearchString(searchString: string): DescriptorState;
  withProjects(projects: Data): DescriptorState;
  withGroupBy(groupBy: GroupBy): DescriptorState;
  withFilterByRepoType(repoType: RepoType[]): DescriptorState;
  withFilterByUsers(users: string[]): DescriptorState;
}