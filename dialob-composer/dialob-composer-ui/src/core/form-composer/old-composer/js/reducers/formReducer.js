import Immutable from 'immutable';
import * as Actions from '../actions/constants';
import {isGlobalValueSet} from '../helpers/utils';
import {camelCase} from 'lodash'

const INITIAL_STATE = Immutable.Map();

function generateItemId(state, type, view) {
  const prefix = view ? camelCase(view) : type;
  let idx = 1;
  let data = state.get('data') || Immutable.Map();
  let variables = state.get('variables') || Immutable.List();
  while (state && (state.get('data').has(`${prefix}${idx}`) || variables.findIndex(v => v.get('name') === `${prefix}${idx}`) > -1)) {
    idx++;
  }
  return `${prefix}${idx}`;
}

function generateValueSetId(state, prefix) {
  let idx = 1;
  while (state && state.find(vs => vs.get('id') === `${prefix}${idx}`)) {
    idx++;
  }
  return `${prefix}${idx}`;
}

function addItem(state, action) {
  const itemId = generateItemId(state, action.config.type, action.config.view);
  const itemConfig = Immutable.fromJS(Object.assign({id: itemId}, action.config));
  return state.update('data', data => data.set(itemId, itemConfig)
              .update(action.parentItemId, parent => {
                if (action.afterItemId) {
                  return parent.update('items', items => items ? items.insert(items.findIndex(i => i === action.afterItemId) + 1, itemId) : Immutable.List([itemId]));
                } else {
                  return parent.update('items', items => items ? items.push(itemId) : Immutable.List([itemId]));
                }
              }))
              .setIn(['metadata', 'composer', 'transient', 'lastItem'], itemConfig);

}

function deleteItem(state, itemId) {
  // Find children and eliminate them
  const collect = target => {
    let subItems = state.getIn(['data', target, 'items']);
    if (subItems) {
      return [target].concat(subItems.toJS().reduce((l, i) => l.concat(collect(i)), []));
    } else {
      return [target];
    }
  };

  let toDelete = collect(itemId);

  // Collect && delete valueSets; skip global value sets
  const globalValueSets = state.getIn(['metadata', 'composer', 'globalValueSets']);
  let valueSets = toDelete.map(itemId => state.getIn(['data', itemId, 'valueSetId']))
    .filter(vsId => vsId)
    .filter(vsId => !isGlobalValueSet(globalValueSets, vsId));
  let newState = state;
  valueSets.forEach(vsId => newState = newState.update('valueSets', v => v.delete(v.findIndex(i => i.get('id') === vsId))));

  newState = newState.update('data', s => toDelete.reduce((i, k) => i.delete(k), s));
  // return state.deleteAll(toDelete); // Immutable 4.0

  // Remove parent ref also
  const parent = newState.get('data').find(v => v.get('items') && v.get('items').contains(itemId));
  return parent ? newState.updateIn(['data', parent.get('id'), 'items'], items => items.delete(items.indexOf(itemId)))
     : newState;
}

function newValueSet(state, itemId = null, entries = null) {
  const valueSetId = generateValueSetId(state.get('valueSets'), 'vs');
  const valueSet = Immutable.fromJS({
    id: valueSetId,
    entries: entries || []
  });
  let newState = state.update('valueSets', valueSets => {
    if (!valueSets) {
      return new Immutable.List([valueSet])
    } else {
      return valueSets.push(valueSet);
    }
  });

  if (itemId) {
    newState = newState.setIn(['data', itemId, 'valueSetId'], valueSetId);
  } else {
    newState = newState.updateIn(['metadata', 'composer', 'globalValueSets'], gvs => {
      const gvsInfo = Immutable.fromJS({
        valueSetId,
        label: ''
      });
      if (!gvs) {
        return new Immutable.List([gvsInfo]);
      } else {
        return gvs.push(gvsInfo);
      }
    });
  }

  return newState;
}

function makeValuesetGlobal(state, valueSetId) {
  let newState = state.updateIn(['metadata', 'composer', 'globalValueSets'], gvs => {
    const gvsInfo = Immutable.fromJS({
      valueSetId,
      label: ''
    });
    if (!gvs) {
      return new Immutable.List([gvsInfo]);
    } else {
      return gvs.push(gvsInfo);
    }
  });
  return newState;
}

function copyValuesetLocal(state, valueSetId, itemId) {
  const newValueSetId = generateValueSetId(state.get('valueSets'), 'vs');
  let newState = state.setIn(['data', itemId, 'valueSetId'], newValueSetId);
  const idx =  findValuesetIndex(state, valueSetId);
  const sourceValueSet = state.get('valueSets').get(idx);
  const newValueSet = sourceValueSet.set('id', newValueSetId);
  newState = newState.update('valueSets', valueSets => valueSets.push(newValueSet));
  return newState;
}

function setValueSetName(state, valueSetId, name) {
  const gvsIndex = state.getIn(['metadata', 'composer', 'globalValueSets']).findIndex(vs => vs.get('valueSetId') === valueSetId);
  if (gvsIndex > -1) {
    return state.setIn(['metadata', 'composer', 'globalValueSets', gvsIndex, 'label'], name);
  } else {
    return state;
  }
}

function findValuesetIndex(state, valueSetId) {
  const valueSets = state.get('valueSets');
  return valueSets ? valueSets.findIndex(s => s.get('id') === valueSetId) : -1;
}

function newValueSetEntry(state, valueSetId) {
  const index = findValuesetIndex(state, valueSetId);
  if (index === -1) {
    return state;
  } else {
    return state.updateIn(['valueSets', index, 'entries'], entries => {
      let entry = Immutable.fromJS({
        id: '',
        label: {}
      });
      if (!entries) {
        return new Immutable.List([entry])
      } else {
        return entries.push(entry)
      }
    })
  }
}

function setValuesetEntries(state, valueSetId, newEntries) {
  const index = findValuesetIndex(state, valueSetId);
  if (index === -1) {
    return state;
  } else {
    return state.updateIn(['valueSets', index, 'entries'], entries => {
      return Immutable.fromJS(newEntries);
    });
  }
}

function updateValuesetEntry(state, valueSetId, index, id, label, language) {
  const vsIndex = findValuesetIndex(state, valueSetId);
  if (vsIndex === -1) {
    return state;
  }
  if (id !== null) {
    return state.setIn(['valueSets', vsIndex, 'entries', index, 'id'], id);
  } else if (label !== null) {
    return state.setIn(['valueSets', vsIndex, 'entries', index, 'label', language], label);
  }
  return state;
}

function deleteValuesetEntry(state, valueSetId, index) {
  const vsIndex = findValuesetIndex(state, valueSetId);
  if (vsIndex === -1) {
    return state;
  }
  return state.deleteIn(['valueSets', vsIndex, 'entries', index]);
}

function moveValuesetEntry(state, valueSetId, from, to) {
  const vsIndex = findValuesetIndex(state, valueSetId);
  if (vsIndex === -1) {
    return state;
  }
  const entry = state.getIn(['valueSets', vsIndex, 'entries', from]);
  return state.updateIn(['valueSets', vsIndex, 'entries'], entries =>
      entries.splice(from, 1).splice(to > from ? to - 1 : to, 0, entry)
  );
}

function updateValueSetEntryAttr(state, valueSetId, id, attr, value) {
  const vsIndex = findValuesetIndex(state, valueSetId);
  return state.updateIn(['valueSets', vsIndex, 'entries'], entries =>
    entries.setIn([entries.findIndex(e => e.get('id') === id), attr], value)
  );
}

function newVariable(state, context = false) {
  const variableId = generateItemId(state, context ? 'context' : 'var');
  const variable = Immutable.fromJS(context ?
    {
      name: variableId,
      context: true,
      contextType: 'text'
    } :
    {
      name: variableId,
      expression: ''
    }
  );
  return state.update('variables', variables => {
    if (!variables) {
      return new Immutable.List([variable]);
    } else {
      return variables.push(variable);
    }
  });
}

function updateVariable(state, id, attribute, value) {
  const variableIndex = state.get('variables').findIndex(v => v.get('name') === id);
  if (variableIndex === -1) {
    return state;
  }
  return state.setIn(['variables', variableIndex, attribute], value);
}

function deleteVariable(state, id) {
  const variableIndex = state.get('variables').findIndex(v => v.get('name') === id);
  if (variableIndex === -1) {
    return state;
  }
  return state.deleteIn(['variables', variableIndex]);
}

function createValidation(language) {
  let validation = {
    message: {},
    rule: ''
  };
  validation.message[language] = '';
  return Immutable.fromJS(validation);
}

function addItemProperty(state, itemId, propKey, value) {
  return state.updateIn(['data', itemId, 'props'], props => {
    if (!props) {
      return new Immutable.Map([[propKey, value]]);
    } else {
      return props.set(propKey, value);
    }
  });
}

function updateItemProperty(state, itemId, propKey, value) {
  return state.updateIn(['data', itemId, 'props'], props => {
    if (props && props.get(propKey) !== undefined) {
      return props.set(propKey, value);
    } else {
      return props;
    }
  });
}

function moveItem(state, fromIndex, toIndex, fromParent, toParent, itemId) {
  return state.updateIn(['data', fromParent, 'items'], items => items.delete(fromIndex))
              .updateIn(['data', toParent, 'items'], items => !items ? new Immutable.List([itemId]) : items.insert(toIndex, itemId));
}

function copyLanguage(state, languageFrom, languageTo) {
  // Items
  let formData = state.get('data').map(item =>
      item.update('label', label => label && label.set(languageTo, label.get(languageFrom))) // Item labels
        .update('description', description => description ? description.set(languageTo, description.get(languageFrom)) : description) // Item description
        .update('validations', validations => validations ? validations.map(v => v.update('message', msg => msg.set(languageTo, msg.get(languageFrom)))) : validations) // Validation messages
    );
  // Valueset entry labels
  let newState = state.get('valueSets') ?  state.update('valueSets', valuesets =>
    valuesets.map(valueset =>
      valueset.update('entries', entries =>
        entries.map(entry => entry.update('label', label => label.set(languageTo, label.get(languageFrom))))
      )
    )
  ) : state;

  // Metadata
  return newState.set('data', formData)
              .updateIn(['metadata', 'languages'], languages => languages.push(languageTo));
}

function deleteLanguage(state, language) {
  // Items
  let formData = state.get('data').map(item =>
    item.update('label', label => label && label.delete(language))
        .update('description', description => description && description.delete(language))
        .update('validations', validations => validations && validations.map(v => v.update('message', msg => msg.delete(language))))
  );
  // Valueset entry labels
  let newState = state.get('valueSets') ? state.update('valueSets', valuesets =>
    valuesets.map(valueset => valueset.update('entries', entries =>
      entries.map(entry => entry.update('label', label => label.delete(language)))
    ))
  ) : state;
  // Metadata
  return newState.set('data', formData)
    .updateIn(['metadata', 'languages'], languages => languages.delete(languages.indexOf(language)));
}

function convertItem(state, itemId, config) {
  return state.updateIn(['data', itemId], item => {
    return item
            .set('type', config.type)
            .set('view', config.view)
            .update('props', p => { if (p) { return p.merge(Immutable.fromJS(config.props)); } else { return Immutable.fromJS(config.props);}})
            .update('className', p => { if (p) {
              return p.concat(Immutable.List(config.className).filter(c => !p.includes(c)));
            } else { return Immutable.fromJS(config.className);}});
  });
}

export function formReducer(state = INITIAL_STATE, action) {
  switch (action.type) {
    case Actions.SET_FORM:
      const newState = Immutable.fromJS(action.formData);
      return action.tagName ? newState.set('_tag', action.tagName) : newState;
    case Actions.SET_FORM_REVISION:
      return state.set('_rev', action.revision);
    default:
      //NOP:
  }

  if (state.get('_tag')) {
    // No editing if not on "LATEST"
    return state;
  }

  switch (action.type) {
    case Actions.ADD_ITEM:
      return addItem(state, action);
    case Actions.UPDATE_ITEM:
      return action.language ? state.setIn(['data', action.itemId, action.attribute, action.language], action.value)
                             : state.setIn(['data', action.itemId, action.attribute], action.value);
    case Actions.CHANGE_ITEM_TYPE:
      return convertItem(state, action.itemId, action.config.config);
    case Actions.DELETE_ITEM:
     // return state.update('data', data => deleteItem(data, action.itemId));
      return deleteItem(state, action.itemId);
    case Actions.CREATE_VALUESET:
      return newValueSet(state, action.forItem, action.entries);
    case Actions.MAKE_VALUESET_GLOBAL:
      return makeValuesetGlobal(state, action.valueSetId);
    case Actions.COPY_VALUESET_LOCAL:
      return copyValuesetLocal(state, action.valueSetId, action.itemId);
    case Actions.SET_VALUESET_ENTRIES:
      return setValuesetEntries(state, action.valueSetId, action.entries);
    case Actions.CREATE_VALUESET_ENTRY:
      return newValueSetEntry(state, action.valueSetId);
    case Actions.UPDATE_VALUESET_ENTRY:
      return updateValuesetEntry(state, action.valueSetId, action.index, action.id, action.label, action.language);
    case Actions.DELETE_VALUESET_ENTRY:
      return deleteValuesetEntry(state, action.valueSetId, action.index);
    case Actions.MOVE_VALUESET_ENTRY:
      return moveValuesetEntry(state, action.valueSetId, action.from, action.to);
    case Actions.UPDATE_VALUESET_ENTRY_ATTR:
      return updateValueSetEntryAttr(state, action.valueSetId, action.id, action.attr, Immutable.fromJS(action.value));
    case Actions.SET_METADATA_VALUE:
      if (action.value) {
        return state.setIn(['metadata', action.attribute], Immutable.fromJS(action.value));
      } else {
        return state.deleteIn(['metadata', action.attribute]);
      }
    case Actions.CREATE_CONTEXT_VARIABLE:
      return newVariable(state, true);
    case Actions.CREATE_EXPRESSION_VARIABLE:
      return newVariable(state);
    case Actions.UPDATE_VARIABLE:
      return updateVariable(state, action.id, action.attribute, action.value);
    case Actions.DELETE_VARIABLE:
      return deleteVariable(state, action.id);
    case Actions.SET_CONTEXT_VALUE:
      return state.setIn(['metadata', 'composer', 'contextValues', action.id], action.value);
    case Actions.CREATE_VALIDATION:
      return state.updateIn(['data', action.itemId],
        item => item.update('validations',
          validations => validations ?  validations.push(createValidation(action.language)) : Immutable.List([createValidation(action.language)])));
    case Actions.DELETE_VALIDATION:
      return state.updateIn(['data', action.itemId],
        item => item.update('validations',
          validations => validations.delete(action.index)));
    case Actions.UPDATE_VALIDATION:
      return state.updateIn(['data', action.itemId],
        item => item.update('validations',
          validations => action.language ?
                          validations.setIn([action.index, action.attribute, action.language], action.value)
                          : validations.setIn([action.index, action.attribute], action.value)
                        ));
      return state;
    case Actions.SET_GLOBAL_VALUESET_NAME:
      return setValueSetName(state, action.valueSetId, action.name);
    case Actions.ADD_LANGUAGE:
      if (action.copyFrom) {
        return copyLanguage(state, action.copyFrom, action.language);
      } else {
        return state.updateIn(['metadata', 'languages'], languages =>
          languages ? languages.push(action.language) : Immutable.List([action.language]));
      }
    case Actions.DELETE_LANGUAGE:
      return deleteLanguage(state, action.language);
    case Actions.ADD_ITEM_PROP:
      return addItemProperty(state, action.itemId, action.propKey, action.value);
    case Actions.UPDATE_ITEM_PROP:
      return updateItemProperty(state, action.itemId, action.propKey, action.value);
    case Actions.DELETE_ITEM_PROP:
      return state.deleteIn(['data', action.itemId, 'props', action.propKey]);
    case Actions.MOVE_ITEM:
      return moveItem(state, action.dragIndex, action.hoverIndex, action.dragParent, action.hoverParent, action.itemId);
    default:
      //NOP:
  }
  return state;
}
