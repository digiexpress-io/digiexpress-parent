import React from 'react';


export interface ProjectIdContextType {
  projectId: string;
  setProjectId(projectId: string): void
}
export const ProjectIdContext = React.createContext<ProjectIdContextType>({
  projectId: '',
  setProjectId(_projectId: string) {    
  }
});


const ProjectIdProvider: React.FC<{ children: React.ReactNode, projectId: string }> = ({ children, projectId: init}) => {
  const [projectId, setProjectId] = React.useState<string>(init);

  const contextValue: ProjectIdContextType = React.useMemo(() => {
    return { projectId, setProjectId };
  }, [projectId]);
  
  return (<ProjectIdContext.Provider value={contextValue}>{children}</ProjectIdContext.Provider>);
};


export { ProjectIdProvider };

