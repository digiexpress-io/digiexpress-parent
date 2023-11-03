import {combineReducers} from 'redux';
import {editorReducer} from './editorReducer';
import {formReducer} from './formReducer';
import {configReducer} from './configReducer';

const reducers = {
  form: formReducer,
  editor: editorReducer,
  config: configReducer,
};

export default function createDialobComposerReducer() {
  return combineReducers(reducers);
}

type EditorType =
  "activeItemId" |
  "activePageId" |
  "activeLanguage" |

  "changeId" |
  "confirmableAction" |
  "itemOptions" |
  "formOptions" |
  
  "errors" |
  "loaded" |
  "newTagDialog" |
  
  "previewContextDialog" |
  "rootItemId" |
  "status" |
  "translationOpen" |
  "treeCollapse" |
      
  "versioningDialog" | 
  "versions" | 
  "variablesDialog";
  
type FormType = "_id" | "_tag" | "name" | "variables" | "data" | "valueSets" | "metadata";
/** 
['data', this.activeItemId]
['data', this.activeItemId, 'validations']
['metadata', 'composer', 'globalValueSets']
['metadata', 'composer', 'contextValues']
['metadata', 'languages']
['metadata', 'label']
*/

type ConfigType = "valueSetProps" | "itemTypes" | "itemEditors";

type ReducerState = {
  dialobComposer: {
    editor?: {
      get: (key: EditorType) => any;
    },
    form: {
      get: (key: FormType) => any;
    },
    config: {
      get: (key: ConfigType) => any;
    }
  }
}

export type {
  ReducerState, FormType, ConfigType
  
}