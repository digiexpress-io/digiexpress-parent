import produce from 'immer';
import camelCase from 'lodash.camelcase';
import { ComposerAction } from './actions';
import { ComposerState, DialobItemTemplate, ComposerCallbacks, ValueSetEntry, ContextVariableType, ContextVariable, Variable, isContextVariable, ValidationRule } from './types';

export const generateItemIdWithPrefix = (state: ComposerState, prefix: string): string => {
  const idList = Object.keys(state.data).concat(state.variables?.map(v => v.name) || []);
  const matcher = `^(${prefix})(\\d*)$`;
  const existing = idList.filter(id => {
      const r = RegExp(matcher);
      return r.test(id);
  });
  let idx = 1;
  while (existing.findIndex(v => v === `${prefix}${idx}`) > -1) {
    idx++;
  }
  return `${prefix}${idx}`;
}

export const generateItemId = (state: ComposerState, itemTemplate: DialobItemTemplate): string => {
  const prefix = itemTemplate.view ? camelCase(itemTemplate.view) : itemTemplate.type;
  return generateItemIdWithPrefix(state, prefix);
};

export const generateValueSetId = (state: ComposerState, prefix = 'vs'): string => {
  let idx = 1;
  while (state.valueSets && state.valueSets.findIndex(vs => vs.id === `${prefix}${idx}`) > -1) {
    idx++;
  }
  return `${prefix}${idx}`;
}

const addItem = (state: ComposerState, itemTemplate: DialobItemTemplate, parentItemId: string, afterItemId?: string, callbacks ?: ComposerCallbacks): void => {
  const id = generateItemId(state, itemTemplate);
  state.data[id] = Object.assign(itemTemplate, {id});
  // TODO: Sanity check if parentItemId exists in form
  const newIndex = state.data[parentItemId].items?.findIndex(i => i === afterItemId);
  if (newIndex === undefined) {
    state.data[parentItemId].items = [id];
  } else if (newIndex < 0) {
    state.data[parentItemId].items?.push(id);
  } else {
    state.data[parentItemId].items?.splice(newIndex + 1, 0, id);
  }

  const onAddItem = callbacks?.onAddItem;
  if (onAddItem) {
    onAddItem(state, state.data[id]);
  }
}

const updateItem = (state: ComposerState, itemId: string, attribute: string, value: any, language ?: string, ): void => {
  // TODO: Sanity: item exists
  // TODO: Sanity: language exists in form level
  // TODO: Sanity: attribute is not an id or type
  if (language) {
    if (state.data[itemId][attribute] === undefined) {
      state.data[itemId][attribute] = {[language]: value};
    } else {
      state.data[itemId][attribute][language] = value;
    }
  } else {
    state.data[itemId][attribute] = value;
  }
}

const convertItem = (state: ComposerState, itemId: string, itemTemplate: DialobItemTemplate): void => {
  const item = state.data[itemId];
  item.type = itemTemplate.type;
  item.view = itemTemplate.view;
  if (item.props) {
    Object.assign(item.props, itemTemplate.props);
  } else {
    item.props = itemTemplate.props;
  }
  if (itemTemplate.className) {
    if (Array.isArray(item.className)) {
      item.className = item.className.concat(itemTemplate.className.filter(className => item.className && item.className.indexOf(className) < 0));
    } else {
      item.className = itemTemplate.className;
    }
  }
}

const deleteItem = (state: ComposerState, itemId: string): void => {
  // TODO: Sanity: item exists

  // Collect item and children to delete
  const collect = (target:string):string[] => {
    let subItems = state.data[target].items;
    if (subItems) {
      return [target].concat(subItems.reduce((l, i) => l.concat(collect(i)), [] as string[]));
    } else {
      return [target];
    }
  }
  const toDelete = collect(itemId);

  // Collect non-global valuesets for to-be deleted items
  const globalValueSets = state.metadata?.composer?.globalValueSets || [];
  const valueSets = toDelete.map(iid => state.data[iid].valueSetId)
    .filter(vsId => vsId)
    .filter(vsId => globalValueSets.findIndex(gvs => gvs.valueSetId === vsId) === -1)
    
  // Delete items
  toDelete.forEach(itemId => {
    delete state.data[itemId];
  });

  // Delete valuesets
  valueSets.forEach(vsId => {
    const vsIdx = state.valueSets ? state.valueSets.findIndex(vs => vs.id === vsId) : -1;
    if (vsIdx > -1 && state.valueSets) {
      state.valueSets.splice(vsIdx, 1);
    }
  });

  // Delete parent ref
  for (const [parentItemId, item] of Object.entries(state.data)) {
    const itemIndex = item.items ? item.items.findIndex(i => i === itemId) : -1;
    if (itemIndex > -1 && item.items) {
      item.items.splice(itemIndex, 1);
      break;
    }
  }
}

const setItemProp = (state: ComposerState, itemId: string, key: string, value: any): void => {
  // TODO: Sanity: item exists
  if (state.data[itemId].props === undefined) {
    state.data[itemId].props = {[key]: value};
  } else {
    const props = state.data[itemId].props;
    if (props !== undefined) {
     props[key] = value;
    }
  }
}

const deleteItemProp = (state: ComposerState, itemId: string, key: string): void => {
  // TODO: Sanity item exists
  const props = state.data[itemId]?.props;
  if (props !== undefined) {
    delete props[key];

    if (Object.keys(props).length === 0) {
      delete state.data[itemId].props;
    }
  }
}

const createValidation = (state: ComposerState, itemId: string, rule ?: ValidationRule): void => {
  const emptyRule: ValidationRule = {message: {}, rule: ''};

  // TODO: Sanity: item exists
  if (state.data[itemId].validations === undefined) {
    state.data[itemId].validations = [rule ? rule : emptyRule];
  } else {
    state.data[itemId].validations?.push(rule ? rule : emptyRule);
  }
}

const setValidationMessage = (state: ComposerState, itemId: string, index: number, language: string, message: string): void => {
  const validations = state.data[itemId].validations;
  if (validations) {
    const rule = validations[index];
    if (!rule) {
      return;
    }
    if (!rule.message) {
      rule.message = {[language]: message};
    } else {
      rule.message[language] = message;
    }
  }
}

const setValidationExpression = (state: ComposerState, itemId: string, index: number, expression: string): void => {
  const validations = state.data[itemId].validations;
  if (validations) {
    const rule = validations[index];
    if (!rule) {
      return;
    }
   rule.rule = expression;
  }
}

const deleteValidation = (state: ComposerState, itemId: string, index: number): void => {
  const validations = state.data[itemId].validations;
  if (validations) {
    const rule = validations[index];
    if (!rule) {
      return;
    }
   validations.splice(index, 1);
  }
}

const moveItem = (state: ComposerState, itemId: string, fromIndex: number, toIndex: number, fromParent: string, toParent:string): void => {
  // TODO: Sanity: item exists
  // TODO: Ergonomics: fromParent and fromIndex can be computed by itemId and not needed in function parameters
  state.data[fromParent]?.items?.splice(fromIndex, 1);
  if (state.data[toParent].items === undefined) {
    state.data[toParent].items = [itemId];
  } else {
    state.data[toParent]?.items?.splice(toIndex, 0, itemId);
  }
}

const createValueSet = (state: ComposerState, itemId: string | null,  entries?: ValueSetEntry[]): void => {
  // TODO: Sanity: item exists if not null

  const valueSetId = generateValueSetId(state, 'vs');
  const valueSet = {
    id: valueSetId,
    entries: entries || []
  };

  if (!state.valueSets) {
    state.valueSets = [valueSet];
  } else {
    state.valueSets.push(valueSet);
  }

  if (itemId) {
    // Local valueset
    state.data[itemId].valueSetId = valueSetId;
  } else {
    // Global valueset
    if (!state.metadata.composer) {
      state.metadata.composer = {};
    }
    if (!state.metadata.composer.globalValueSets) {
      state.metadata.composer.globalValueSets = [];
    }
    state.metadata.composer.globalValueSets.push({valueSetId, label: ''})
  }
}

const setValueSetEntries = (state: ComposerState, valueSetId: string, entries: ValueSetEntry[]): void => {
  // TODO: Sanity: valueSet exists

  if (state.valueSets) {
    const vsIdx = state.valueSets.findIndex(vs => vs.id === valueSetId);
    if (vsIdx > -1) {
      state.valueSets[vsIdx].entries = entries;
    } 
  }
}

const addValueSetEntry = (state: ComposerState, valueSetId: string, entry?: ValueSetEntry): void => {
  // TODO Sanity: valueSet extists

  if (state.valueSets) {
    const vsIdx = state.valueSets.findIndex(vs => vs.id === valueSetId);
    if (vsIdx > -1) {
      const newEntry = entry ? entry : { id: '', label: {} };
      state.valueSets[vsIdx].entries.push(newEntry);
    }
  }
}

const updateValueSetEntry = (state: ComposerState, valueSetId: string, index: number, entry: ValueSetEntry): void => {
  // TODO Sanity: ValueSet exists
  // TODO Sanity: Entry index exists

  if (state.valueSets) {
    const vsIdx = state.valueSets.findIndex(vs => vs.id === valueSetId);
    if (vsIdx > -1) {
      state.valueSets[vsIdx].entries[index] = entry;
    }
  }
}

const deleteValueSetEntry = (state: ComposerState, valueSetId: string, index: number): void => {
  // TODO Sanity: ValueSet exists
  // TODO Sanity: Entry index exists

  if (state.valueSets) {
    const vsIdx = state.valueSets.findIndex(vs => vs.id === valueSetId);
    if (vsIdx > -1) {
      state.valueSets[vsIdx].entries.splice(index, 1);
    }
  }
}

const moveValueSetEntry = (state: ComposerState, valueSetId: string, from: number, to: number): void => {
  // TODO Sanity: ValueSet exists
  // TODO Sanity: Entry index exists

  if (state.valueSets) {
    const vsIdx = state.valueSets.findIndex(vs => vs.id === valueSetId);
    if (vsIdx > -1) {
      const newIndex = to > state.valueSets[vsIdx].entries.length ? state.valueSets[vsIdx].entries.length - 1 : to;
      state.valueSets[vsIdx].entries.splice(newIndex, 0, state.valueSets[vsIdx].entries.splice(from, 1)[0]);
    }
  }
}

const setGlobalValueSetName = (state: ComposerState, valueSetId: string, name: string): void => {
  if (state.metadata?.composer?.globalValueSets) {
    const gvsIdx = state.metadata.composer.globalValueSets.findIndex(gvs => gvs.valueSetId === valueSetId);
    if (gvsIdx > -1) {
      state.metadata.composer.globalValueSets[gvsIdx].label = name;
    }
  }
}

const setMetadataValue = (state: ComposerState, attr: string, value: any): void => {
  // TODO: Sanity: Prevent overwriting certain critical attributes

  state.metadata[attr] = value;
}

const createVariable = (state: ComposerState, context: boolean): void => {
  const variableId = generateItemIdWithPrefix(state, context ? 'context' : 'var');

  const variable: (ContextVariable | Variable) = context ? {
    name: variableId,
    context: true,
    contextType: 'text'
  } : {
    name: variableId,
    expression: ''
  };
  
  if (!Array.isArray(state.variables)) {
    state.variables = [variable];
  } else {
    state.variables.push(variable);
  }
}

const updateContextVariable = (state: ComposerState, variableId: string, contextType ?: ContextVariableType, defaultValue ?: any): void => {
  if (state.variables) {
    const varIdx = state.variables.findIndex(v => isContextVariable(v) && v.name === variableId);
    if (varIdx > -1) {
      if (contextType !== undefined) {
        (state.variables[varIdx] as ContextVariable).contextType = contextType;
      }
      if (defaultValue !== undefined) {
        (state.variables[varIdx] as ContextVariable).defaultValue = defaultValue;
      }
    }
  }
}

const updateExpressionVariable = (state: ComposerState, variableId: string, expression: string): void => {
  if (state.variables) {
    const varIdx = state.variables.findIndex(v => !isContextVariable(v) && v.name === variableId);
    if (varIdx > -1) {
      (state.variables[varIdx] as Variable).expression = expression;
    }
  }
}

const deleteVariable = (state: ComposerState, variableId: string): void => {
  if (state.variables) {
    const varIdx = state.variables.findIndex(v => v.name === variableId);
    if (varIdx > -1) {
      state.variables.splice(varIdx, 1);
    }
  }
}

const addLanguage = (state: ComposerState, language: string, copyFrom?: string): void => {
  if (state.metadata.languages && state.metadata.languages.indexOf(language) > -1) {
    // Already exists, NO-OP
    return;
  }
  
  // Copy language
  if (copyFrom && copyFrom !== language) {
    // Items
    for (const [itemId, item] of Object.entries(state.data)) {
      // Item label
      if (item.label) {
        item.label[language] = item.label[copyFrom];
      }
      // Item description
      if (item.description) {
        item.description[language] = item.description[copyFrom];
      }
      // Item validation messages
      if (item.validations) {
        item.validations.forEach(validation => {
          if (validation.message) {
            validation.message[language] = validation.message[copyFrom];
          }
        })
      }
    }
    // Valueset entry labels
    if (state.valueSets) {
      state.valueSets.forEach(vs => {
        vs.entries.forEach(vse => {
          if (vse.label) {
            vse.label[language] = vse.label[copyFrom];
          }
        })
      })
    }
  }

  // Metadata 
  if (state.metadata.languages) {
    state.metadata.languages.push(language);
  } else {
    state.metadata.languages = [language];
  }
}

const deleteLanguage = (state: ComposerState, language: string): void => {
  for (const [itemId, item] of Object.entries(state.data)) {
    if (item.label) {
      delete item.label[language];
    }
    if (item.description) {
      delete item.description[language];
    }
    if (item.validations) {
      item.validations.forEach(v => {
        if (v.message) {
          delete v.message[language];
        }
      })
    }
  }

  if (state.valueSets) {
    state.valueSets.forEach(vs => {
      vs.entries.forEach(vse => {
        if (vse.label[language]) {
          delete vse.label[language];
        }        
      });
    });
  }

  if (state.metadata.languages) {
    const langIdx = state.metadata.languages.indexOf(language);
    if (langIdx > -1) {
      state.metadata.languages.splice(langIdx, 1);
    }
  }
}

export const formReducer = (state: ComposerState, action: ComposerAction, callbacks ?: ComposerCallbacks): ComposerState => {
  const newState = produce(state, state => {
    if (action.type === 'addItem') {
      addItem(state, action.config, action.parentItemId, action.afterItemId, callbacks);
    } else if (action.type === 'updateItem') {
      updateItem(state, action.itemId, action.attribute, action.value, action.language);
    } else if (action.type === 'changeItemType') {
      convertItem(state, action.itemId, action.config);
    } else if (action.type === 'deleteItem') {
      deleteItem(state, action.itemId);
    } else if (action.type === 'setItemProp') {
      setItemProp(state, action.itemId, action.key, action.value);
    } else if (action.type === 'deleteItemProp') {
      deleteItemProp(state, action.itemId, action.key);
    } else if (action.type === 'createValidation') {
      createValidation(state, action.itemId, action.rule);
    } else if (action.type === 'setValidationMessage') {
      setValidationMessage(state, action.itemId, action.index, action.language, action.message);
    } else if (action.type === 'setValidationExpression') {
      setValidationExpression(state, action.itemId, action.index, action.expression);
    } else if (action.type === 'deleteValidation') {
      deleteValidation(state, action.itemId, action.index);
    } else if (action.type === 'moveItem') {
      moveItem(state, action.itemId, action.fromIndex, action.toIndex, action.fromParent, action.toParent);
    } else if (action.type === 'createValueSet') {
      createValueSet(state, action.itemId, action.entries);
    } else if (action.type === 'setValueSetEntries') {
      setValueSetEntries(state, action.valueSetId, action.entries);
    } else if (action.type === 'addValueSetEntry') {
      addValueSetEntry(state, action.valueSetId, action.entry);
    } else if (action.type === 'updateValueSetEntry') {
      updateValueSetEntry(state, action.valueSetId, action.index, action.entry);
    } else if (action.type === 'deleteValueSetentry') {
      deleteValueSetEntry(state, action.valueSetId, action.index);
    } else if (action.type === 'moveValueSetEntry') {
      moveValueSetEntry(state, action.valueSetId, action.from, action.to);
    } else if (action.type === 'setGlobalValueSetName') {
      setGlobalValueSetName(state, action.valueSetId, action.name);
    } else if (action.type === 'setMetadataValue') {
      setMetadataValue(state, action.attr, action.value);
    } else if (action.type === 'createVariable') {
      createVariable(state, action.context);
    } else if (action.type === 'updateContextVariable') {
      updateContextVariable(state, action.variableId, action.contextType, action.defaultValue);
    } else if (action.type === 'updateExpressionVariable') {
      updateExpressionVariable(state, action.variableId, action.expression);
    } else if (action.type === 'deleteVariable') {
      deleteVariable(state, action.variableId);
    } else if (action.type === 'addLanguage') {
      addLanguage(state, action.language, action.copyFrom);
    } else if (action.type === 'deleteLanguage') {
      deleteLanguage(state, action.language);
    }
  });
  return newState;
}
