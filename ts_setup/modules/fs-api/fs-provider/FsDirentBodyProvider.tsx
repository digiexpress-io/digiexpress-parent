import React from 'react';
import { Fs } from '../fs-types';
import { ItemReferencesEntry } from './FsWorld';
import { useFsDirent } from './FsDirentProvider';




export type { ItemReferencesEntry };

export interface FsDirentBodyContextType {
  body: Fs.WrenchBody;
  refresh(treeId: string): Promise<Fs.WrenchBody>;
}


const FsDirentBodyContext = React.createContext<FsDirentBodyContextType | undefined>(undefined);

export interface FsDirentBodyProviderProps {
  direntId: string;
  children: React.ReactNode;
}

export const FsDirentBodyProvider: React.FC<FsDirentBodyProviderProps> = (props) => {

  const { fetchDirentBody, getDirent } = useFsDirent();
  const dirent = getDirent(props.direntId)!;
  const [body, setBody] = React.useState<Fs.WrenchBody>();

  React.useEffect(function load() {
    fetchDirentBody(props.direntId, dirent.type)
      .then((body) => {
        const wb = body as Fs.WrenchBody;
        setBody(wb);
      });
  }, [props.direntId, dirent.commitIndex!.treeId]);

  const refresh = React.useCallback(async (treeId: string): Promise<Fs.WrenchBody> => {
    if (dirent.commitIndex!.treeId === treeId) {
      return body!;
    }

    const wb = await fetchDirentBody(props.direntId, dirent.type) as Fs.WrenchBody;
    setBody(wb);
    return wb;
  }, [props.direntId, dirent.commitIndex!.treeId, body]);

  if(!body) {
    return (<></>);
  }

  return (
    <FsDirentBodyContext.Provider value={{ body, refresh }}>
      {props.children}
    </FsDirentBodyContext.Provider>
  );
};

export function useFsDirentBody(): FsDirentBodyContextType {
  const result = React.useContext(FsDirentBodyContext);
  if (!result) {
    throw new Error('FsDirentPropsContext is not created!')
  }
  return result;
}
