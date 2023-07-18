import { ResourceContext, ResourceProvider, useContext as useResource, Session } from "./context";
import { createToolbar as createToolbarAs } from "./toolbar";
import { default as ExplorerAs } from "./explorer";

import { Content as ContentAs, ContentProps, createContent as createContentAs } from "./content";
import { ResourceEditor, ResourceEditorProps } from './ResourceEditor'
import { EditType as EditTypeAs, EditTypeProps } from './edit-type';

import { Search as SearchAs, createSearch as createSearchAs } from './search';
import { Releases as ReleasesAs } from './releases';
import { default as DebugAs } from './debug';


declare namespace Hdes {
  export {
    Session,
    ContentProps,
    EditTypeProps,
    ResourceEditorProps as EditorProps,
  };
}

namespace Hdes {
  export const Composer = ResourceEditor;
  
  /*
  export const Search = SearchAs;
  export const Releases = ReleasesAs;
  export const Debug = DebugAs;
  export const EditType = EditTypeAs;
  
  export const Explorer = ExplorerAs;
  export const Content = ContentAs;
  export const Context = ResourceContext;
  export const Provider = ResourceProvider;
  export const useContext = useResource;
  
  export const createContent = createContentAs;
  export const createSearch = createSearchAs;
  export const createToolbar = createToolbarAs;
  */
  
}

export default Hdes;


