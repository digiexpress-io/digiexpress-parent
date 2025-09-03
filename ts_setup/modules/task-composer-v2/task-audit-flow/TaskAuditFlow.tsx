import React from 'react';
import { Box } from '@mui/material';
import Editor from '@monaco-editor/react';
import YAML from 'yaml';


import { useTaskDashboard } from '../task-dashboard';


const toYaml = (props: any) => {
  const doc = new YAML.Document();
  doc.contents = props;
  return doc.toString();
}

export const TaskAuditFlow: React.FC = () => {
  const { taskAudit } = useTaskDashboard();


  return (
    <Box>
      { taskAudit.flow?.processFlowLog ?
        (<Editor
          value={toYaml(taskAudit.flow?.processFlowLog)}
          onChange={() => {}}
          defaultLanguage='yaml'
          height='500px'
        />) : (<>no flow</>)
      }
    </Box>
  );
}


