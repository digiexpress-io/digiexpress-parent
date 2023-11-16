import React from 'react';
import { RepoType } from 'client';

export type ProjectType = RepoType | 'PROJECT';

export interface ProjectIdContextType {
  projectId: string;
  projectType: ProjectType;
  setProjectId(projectId: string, projectType: ProjectType): void
}
export const ProjectIdContext = React.createContext<ProjectIdContextType>({
  projectId: '',
  projectType: 'PROJECT',
  setProjectId(_projectId: string, _projectType: ProjectType) {
  }
});


const ProjectIdProvider: React.FC<{ children: React.ReactNode, projectId: string }> = ({ children, projectId: init }) => {
  const [projectId, setProjectId] = React.useState<string>(init);
  const [projectType, setProjectType] = React.useState<ProjectType>('PROJECT');


  const contextValue: ProjectIdContextType = React.useMemo(() => {
    return {
      projectId, projectType, setProjectId: (projectId: string, projectType: ProjectType) => {
        setProjectId(projectId);
        setProjectType(projectType);
      }
    };
  }, [projectId, projectType]);

  return (<ProjectIdContext.Provider value={contextValue}>{children}</ProjectIdContext.Provider>);
};


export { ProjectIdProvider };

