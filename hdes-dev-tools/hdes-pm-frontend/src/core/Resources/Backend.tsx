
declare namespace Backend {
  
  interface Service {
    users: UserService;
    projects: ProjectService;
    groups: GroupService;

    onUpdate: (listener: (newService: Service) => void) => void;
  }
  
  interface GroupService {
    query: (args?: {top?: number}) => GroupQuery,
  }
  
  interface GroupQuery {
    onSuccess: (handle: (groups: GroupResource[]) => void) => void;
  }
  
  interface ProjectService {
    query: (args?: {top?: number}) => ProjectQuery,    
  }
  
  interface ProjectQuery {
    onSuccess: (handle: (projects: ProjectResource[]) => void) => void;
  }
  
  interface UserService {
    query: (args?: {top?: number}) => UserQuery;
    builder: () => UserBuilder;
    save: (builder: UserBuilder) => { onSuccess: (handle: (user: UserResource) => void) => void; };
  }
  
  interface UserQuery {
    onSuccess: (handle: (users: UserResource[]) => void) => void;
  }
  
  interface ProjectResource {
    project: Project;
    users: Record<string, User>;
    access: Record<string, Access>;
    groups: Record<string, Group>;
    groupUsers: Record<string, GroupUser>;
  }
  
  interface GroupResource {
    group: Group;
    users: Record<string, User>;
    access: Record<string, Access>;
    groupUsers: Record<string, GroupUser>;
    projects: Record<string, Project>;
  }
  
  
  interface UserResource {
    user: User;
    projects: Record<string, Project>;
    access: Record<string, Project>;
    groups: Record<string, Group>;
    groupUsers: Record<string, GroupUser>;
  }

  interface UserBuilder {
    id?: string; 
    name?: string; 
    externalId?: string;
    groups: string[];
    projects: string[];

    withName: (name: string) => UserBuilder;
    withExternalId: (externalId: string) => UserBuilder;
    withProjects: (projects: string[]) => UserBuilder;
    withGroups: (groups: string[]) => UserBuilder;
    from: (from: UserBuilder) => UserBuilder;
  }
  
  interface User {
    id: string;
    rev: string;
    name: string;
    externalId?: string;
    created: Date;
  }
  
  interface Access {
    id: string;
    rev: string;
    created: Date;
    name: string;
    projectId: string;
    groupId?: string;
    userId?: string;
  }

  interface Group {
    id: string;
    rev: string;
    created: Date;
    name: string;
  }

  interface GroupUser {
    id: string;
    rev: string;
    created: Date;
    userId: string;
    groupId: string;
  }

  interface Project {
    id: string;
    rev: string;
    created: Date;
    name: string;
  }
}
export default Backend;