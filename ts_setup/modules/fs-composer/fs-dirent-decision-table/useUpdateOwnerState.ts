import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface UpdateOwnerState {
  isDarkMode: boolean;
  dirent: Fs.DirentBase | undefined;
  name: string;
  wrenchBody: Fs.WrenchBody | undefined;
  decision: Fs.DecisionAst | undefined;
  onChangeName: (value: string) => void;
}

export const useUpdateOwnerState = (props: { direntId: string }): UpdateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { getDirent, fetchDirentBody } = useFsDirent();

  const dirent = getDirent(props.direntId);

  const [name, setName] = React.useState(dirent?.name ?? '');
  const [wrenchBody, setWrenchBody] = React.useState<Fs.WrenchBody | undefined>(undefined);
  const [decision, setDecision] = React.useState<Fs.DecisionAst | undefined>(undefined);

  React.useEffect(() => {
    fetchDirentBody(props.direntId, 'DECISION_TABLE').then((body) => {
      const wb = body as Fs.WrenchBody;
      setWrenchBody(wb);
      setDecision(wb.decisions[props.direntId]?.ast);
    });
  }, [props.direntId]);

  function onChangeName(value: string) {
    setName(value);
  }

  return ({
    isDarkMode,
    dirent,
    name,
    wrenchBody,
    decision,
    onChangeName,
  });
};
