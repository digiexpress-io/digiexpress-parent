import fs from "fs";
import path from "path";
import {generateItemId, formReducer} from '../reducer';
import { ComposerAction } from '../actions';
import { DialobItem, ComposerCallbacks, ComposerState } from '../types';

const testFormFile = path.join(__dirname, "./", "testForm.json");
const testFormContent = fs.readFileSync(testFormFile, "utf8");
const testForm: ComposerState = JSON.parse(testFormContent);

const cleanFormFile = path.join(__dirname, "./", "cleanForm.json");
const cleanFormContent = fs.readFileSync(cleanFormFile, "utf8");
const cleanForm: ComposerState = JSON.parse(cleanFormContent);

test('find next item identifier existing', () => {
  const itemTemplate: DialobItem = {
    id: '',
    type: 'group'
  };
  const newId = generateItemId(testForm, itemTemplate);
  expect(newId).toBe('group10');
});

test('find next item identifier new', () => {
  const itemTemplate: DialobItem = {
    id: '',
    type: 'text',
    view: 'newSomething'
  };
  const newId = generateItemId(testForm, itemTemplate);
  expect(newId).toBe('newSomething1');
});

test('Add new item, afterCertain', () => {
  const action: ComposerAction = {
    type: 'addItem',
    config: {
      id: '',
      type: 'text'
    },
    parentItemId: 'group15',
    afterItemId: 'lob1'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.text1).toBeDefined();
  expect(newState.data.group15.items).toStrictEqual(
    ['exposeCompanyExists',
    'companyName',
    'companyID',
    'newCompany',
    'numberOfEmployees',
    'lob1',
    'text1',
    'otherBL',
    'nameOfOtherBL',
    'facilityType',
    'propertyInCar',
    'evaluatedRiskType']);
});

test('Add new item 1, at end', () => {
  const action: ComposerAction = {
    type: 'addItem',
    config: {
      id: '',
      type: 'text'
    },
    parentItemId: 'group15'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.text1).toBeDefined();
  expect(newState.data.group15.items).toStrictEqual(
    ['exposeCompanyExists',
    'companyName',
    'companyID',
    'newCompany',
    'numberOfEmployees',
    'lob1',
    'otherBL',
    'nameOfOtherBL',
    'facilityType',
    'propertyInCar',
    'evaluatedRiskType',
    'text1']);
});

test('Add new item, at end', () => {
  const action: ComposerAction = {
    type: 'addItem',
    config: {
      id: '',
      type: 'text'
    },
    parentItemId: 'group15'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.text1).toBeDefined();
  expect(newState.data.group15.items).toStrictEqual(
    ['exposeCompanyExists',
    'companyName',
    'companyID',
    'newCompany',
    'numberOfEmployees',
    'lob1',
    'otherBL',
    'nameOfOtherBL',
    'facilityType',
    'propertyInCar',
    'evaluatedRiskType',
    'text1']);
});

test('Add new item, callback', () => {
  const action: ComposerAction = {
    type: 'addItem',
    config: {
      id: '',
      type: 'text'
    },
    parentItemId: 'group15'
  };

  const callbacks: ComposerCallbacks = {
    onAddItem: (state: ComposerState, item: DialobItem) => {
      console.log('Called!', item);
    }
  }

  spyOn(callbacks, 'onAddItem').and.callThrough();

  const newState = formReducer(testForm, action, callbacks);
  expect(newState.data.text1).toBeDefined();
  expect(callbacks.onAddItem).toBeCalled();
});

test('Update item, normal existing value', () => {
  const action: ComposerAction = {
    type: 'updateItem',
    itemId: 'tenantAdminLastName',
    attribute: 'required',
    value: false
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.tenantAdminLastName.required).toBe(false);
});

test('Update item, normal existing language value', () => {
  const action: ComposerAction = {
    type: 'updateItem',
    itemId: 'tenantAdminLastName',
    attribute: 'label',
    value: 'Test',
    language: 'fi'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.tenantAdminLastName.label?.fi).toBe('Test');
});

test('Update item, normal new language value', () => {
  const action: ComposerAction = {
    type: 'updateItem',
    itemId: 'tenantAdminLastName',
    attribute: 'description',
    value: 'Test',
    language: 'fi'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.tenantAdminLastName.description?.fi).toBe('Test');
});

test('Change item type with merging the props #1', () => {
  const action: ComposerAction = {
    type: 'changeItemType',
    itemId: 'riskTrainingTypes',
    config: {
      type: 'list',
      className: ['a', 'b'],
      props: {
        notscored: true
      }
    }
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.riskTrainingTypes.type).toBe('list');
  expect(newState.data.riskTrainingTypes.props).toStrictEqual({display: 'dropdown', notscored: true});
  expect(newState.data.riskTrainingTypes.className).toStrictEqual(['a', 'b']);
});

test('Change item type with merging the props #2', () => {
  const action: ComposerAction = {
    type: 'changeItemType',
    itemId: 'cargoSpaceInspectio',
    config: {
      type: 'multichoice',
      className: ['a', 'b']
    }
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.cargoSpaceInspectio.type).toBe('multichoice');
  expect(newState.data.cargoSpaceInspectio.props).toBeUndefined();
  expect(newState.data.cargoSpaceInspectio.className).toStrictEqual(['a', 'c', 'b']);
});

test('Delete item, with local valueset', () => {
  const action: ComposerAction = {
    type: 'deleteItem',
    itemId: 'accidentHistory'
  };

  const newState = formReducer(testForm, action);
  expect(newState.data.accidentHistory).toBeUndefined();
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
   expect(newState.valueSets.findIndex(vs => vs.id === 'vs1')).toEqual(-1);
  }
  expect(newState.data.generalGroup.items).not.toContain('accidentHistory');
});

test('Delete item, keep global valueset', () => {
  const action: ComposerAction = {
    type: 'deleteItem',
    itemId: 'list15gvs'
  };

  const newState = formReducer(testForm, action);
  expect(newState.data.list15gvs).toBeUndefined();
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    expect(newState.valueSets.findIndex(vs => vs.id === 'vs45')).toBeGreaterThan(-1);
  }
  expect(newState.data.group6.items).not.toContain('list15gvs');
});

test('Delete item, delete children', () => {
  const action: ComposerAction = {
    type: 'deleteItem',
    itemId: 'group6'
  };

  const newState = formReducer(testForm, action);
  expect(newState.data.list14).toBeUndefined();
  expect(newState.data.list15).toBeUndefined();
  expect(newState.data.list15gvs).toBeUndefined();
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    expect(newState.valueSets.findIndex(vs => vs.id === 'vs45')).toBeGreaterThan(-1); // Keep global VS
    expect(newState.valueSets.findIndex(vs => vs.id === 'vs53')).toEqual(-1); 
    expect(newState.valueSets.findIndex(vs => vs.id === 'vs54')).toEqual(-1); 
  }
  expect(newState.data.vehicleGroup.items).not.toContain('group6');
});

test('Set item prop, existing prop', () => {
  const action: ComposerAction = {
    type: 'setItemProp',
    itemId: 'cyberGroup',
    key: 'columns',
    value: 2
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.cyberGroup?.props?.columns).toEqual(2);
});

test('Set item prop, new prop', () => {
  const action: ComposerAction = {
    type: 'setItemProp',
    itemId: 'tenantAdminLastName',
    key: 'testKey',
    value: {test: 'value', z: 'x'}
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.tenantAdminLastName?.props?.testKey).toStrictEqual({test: 'value', z: 'x'});
});

test('Delete item prop, one of many', () => {
  const action: ComposerAction = {
    type: 'deleteItemProp',
    itemId: 'fireDoorsOk',
    key: 'someother'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.fireDoorsOk.props).toBeDefined();
  if (newState.data.fireDoorsOk?.props) {
    expect(newState.data.fireDoorsOk?.props?.display).toEqual('dropdown');
    expect(Object.keys(newState.data.fireDoorsOk.props).length).toEqual(1);
  }
});

test('Delete item prop, last', () => {
  const action: ComposerAction = {
    type: 'deleteItemProp',
    itemId: 'cyberGroup',
    key: 'columns'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.cyberGroup.props).toBeUndefined();
  expect(newState.data.cyberGroup).toBeDefined();
});

test('Move item, under same parent', () => {
  const action: ComposerAction = {
    type: 'moveItem',
    itemId: 'lob1',
    fromParent: 'group15',
    fromIndex: 5,
    toParent: 'group15',
    toIndex: 3
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.group15?.items?.length).toEqual(11);
  if (newState.data.group15.items) {
    expect(newState.data.group15.items[3]).toEqual('lob1');
    expect(newState.data.group15.items[5]).toEqual('numberOfEmployees');
  }
});

test('Move item, under same parent, to first position', () => {
  const action: ComposerAction = {
    type: 'moveItem',
    itemId: 'lob1',
    fromParent: 'group15',
    fromIndex: 5,
    toParent: 'group15',
    toIndex: 0
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.group15?.items?.length).toEqual(11);
  if (newState.data.group15.items) {
    expect(newState.data.group15.items[0]).toEqual('lob1');
    expect(newState.data.group15.items[5]).toEqual('numberOfEmployees');
  }
});

test('Move item, under same parent, beyond end', () => {
  const action: ComposerAction = {
    type: 'moveItem',
    itemId: 'lob1',
    fromParent: 'group15',
    fromIndex: 5,
    toParent: 'group15',
    toIndex: 105
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.group15?.items?.length).toEqual(11);
  if (newState.data.group15.items) {
    expect(newState.data.group15.items[10]).toEqual('lob1');
    expect(newState.data.group15.items[5]).toEqual('otherBL');
  }
});

test('Move item, to anohter parent', () => {
  const action: ComposerAction = {
    type: 'moveItem',
    itemId: 'lob1',
    fromParent: 'group15',
    fromIndex: 5,
    toParent: 'group16',
    toIndex: 3
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.group15?.items?.length).toEqual(10);
  if (newState.data.group15.items) {
    expect(newState.data.group15.items[5]).toEqual('otherBL');
  }
  expect(newState.data.group16?.items?.length).toEqual(6);
  if (newState.data.group16.items) {
    expect(newState.data.group16.items[3]).toEqual('lob1');
  }
});

test('Move item, to another empty parent', () => {
  const action: ComposerAction = {
    type: 'moveItem',
    itemId: 'lob1',
    fromParent: 'group15',
    fromIndex: 5,
    toParent: 'emptygroup',
    toIndex: 3
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.group15?.items?.length).toEqual(10);
  if (newState.data.group15.items) {
    expect(newState.data.group15.items[5]).toEqual('otherBL');
  }
  expect(newState.data.emptygroup?.items?.length).toEqual(1);
  if (newState.data.emptygroup.items) {
    expect(newState.data.emptygroup.items[0]).toEqual('lob1');
  }
});

test('Create validation rule, empty', () => {
  const action: ComposerAction = {
    type: 'createValidation',
    itemId: 'workRelatedRiskAnalysis'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.workRelatedRiskAnalysis.validations).toBeDefined();
  if (newState.data.workRelatedRiskAnalysis.validations) {
    expect(newState.data.workRelatedRiskAnalysis.validations.length).toEqual(2);
    expect(newState.data.workRelatedRiskAnalysis.validations[1]).toStrictEqual({message: {}, rule: ''});
  }
});

test('Create validation rule, empty, new', () => {
  const action: ComposerAction = {
    type: 'createValidation',
    itemId: 'accidentHistory'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.accidentHistory.validations).toBeDefined();
  if (newState.data.accidentHistory.validations) {
    expect(newState.data.accidentHistory.validations.length).toEqual(1);
    expect(newState.data.accidentHistory.validations[0]).toStrictEqual({message: {}, rule: ''});
  }
});

test('Create validation rule, preset', () => {
  const action: ComposerAction = {
    type: 'createValidation',
    itemId: 'accidentHistory',
    rule: {
      message: { 'en': 'test'},
      rule: 'true'
    }
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.accidentHistory.validations).toBeDefined();
  if (newState.data.accidentHistory.validations) {
    expect(newState.data.accidentHistory.validations.length).toEqual(1);
    expect(newState.data.accidentHistory.validations[0]).toStrictEqual({
                  message: { 'en': 'test'},
                  rule: 'true'
                });
  }
});

test('Set validation rule message, update', () => {
  const action: ComposerAction = {
    type: 'setValidationMessage',
    itemId: 'workRelatedRiskAnalysis',
    index: 0,
    language: 'fi',
    message: 'test'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.workRelatedRiskAnalysis.validations).toBeDefined();
  if (newState.data.workRelatedRiskAnalysis.validations) {
    expect(newState.data.workRelatedRiskAnalysis.validations[0].message?.fi).toEqual('test');
  }
});

test('Set validation rule message, update, new language', () => {
  const action: ComposerAction = {
    type: 'setValidationMessage',
    itemId: 'workRelatedRiskAnalysis',
    index: 0,
    language: 'sv',
    message: 'test'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.workRelatedRiskAnalysis.validations).toBeDefined();
  if (newState.data.workRelatedRiskAnalysis.validations) {
    expect(newState.data.workRelatedRiskAnalysis.validations[0].message?.sv).toEqual('test');
  }
});

test('Set validation rule message, new', () => {
  const action: ComposerAction = {
    type: 'setValidationMessage',
    itemId: 'tenantAdminLastName',
    index: 0,
    language: 'fi',
    message: 'test'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.tenantAdminLastName.validations).toBeDefined();
  if (newState.data.tenantAdminLastName.validations) {
    expect(newState.data.tenantAdminLastName.validations[0].message?.fi).toEqual('test');
  }
});

test('Set validation rule expression', () => {
  const action: ComposerAction = {
    type: 'setValidationExpression',
    itemId: 'workRelatedRiskAnalysis',
    index: 0,
    expression: 'true'
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.workRelatedRiskAnalysis.validations).toBeDefined();
  if (newState.data.workRelatedRiskAnalysis.validations) {
    expect(newState.data.workRelatedRiskAnalysis.validations[0].rule).toEqual('true');
  }
});

test('Delete validation rule', () => {
  const action: ComposerAction = {
    type: 'deleteValidation',
    itemId: 'workRelatedRiskAnalysis',
    index: 0
  };
  const newState = formReducer(testForm, action);
  expect(newState.data.workRelatedRiskAnalysis.validations).toBeDefined();
  if (newState.data.workRelatedRiskAnalysis.validations) {
    expect(newState.data.workRelatedRiskAnalysis.validations.length).toEqual(0);
  }
});

test('Create valueset, local', () => {
  const action: ComposerAction = {
    type: 'createValueSet',
    itemId: 'list15nvs'
  };

  const newState = formReducer(testForm, action);
  expect(newState.data.list15nvs.valueSetId).toEqual('vs40');
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    expect(newState.valueSets.findIndex(vs => vs.id === 'vs40')).toBeGreaterThan(-1);
  }
});

test('Create valueset, local, with entries', () => {
  const action: ComposerAction = {
    type: 'createValueSet',
    itemId: 'list15nvs',
    entries: [
      {id: 'a', label: {'en': 'Test A'}},
      {id: 'b', label: {'en': 'Test B'}}
    ]
  }

  const newState = formReducer(testForm, action);
  expect(newState.data.list15nvs.valueSetId).toEqual('vs40');
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs40');
    expect(vsIndex).toBeGreaterThan(-1);
    expect(newState.valueSets[vsIndex].entries).toBeDefined();
    expect(newState.valueSets[vsIndex].entries[0]).toStrictEqual({id: 'a', label: {'en': 'Test A'}});
    expect(newState.valueSets[vsIndex].entries[1]).toStrictEqual({id: 'b', label: {'en': 'Test B'}});
  }
});

test('Create valueset, global', () => {
  const action: ComposerAction = {
    type: 'createValueSet',
    itemId: null
  };

  const newState = formReducer(testForm, action);
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs40');
    expect(vsIndex).toBeGreaterThan(-1);
  }

  expect(newState.metadata?.composer?.globalValueSets).toBeDefined();
  if (newState.metadata?.composer?.globalValueSets) {
    expect(newState.metadata.composer.globalValueSets.findIndex(gvs => gvs.valueSetId === 'vs40')).toBeGreaterThan(-1);
  }
});

test('Create valueset, global, clean form', () => {
  const action: ComposerAction = {
    type: 'createValueSet',
    itemId: null
  };

  const newState = formReducer(cleanForm, action);
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs1');
    expect(vsIndex).toBeGreaterThan(-1);
  }

  expect(newState.metadata?.composer?.globalValueSets).toBeDefined();
  if (newState.metadata?.composer?.globalValueSets) {
    expect(newState.metadata.composer.globalValueSets.findIndex(gvs => gvs.valueSetId === 'vs1')).toBeGreaterThan(-1);
  }
});

test('Set valueset entries', () => {
  const action: ComposerAction = {
    type: 'setValueSetEntries',
    valueSetId: 'vs2',
    entries: [
      {id: 'a', label: {'en': 'Test A'}},
      {id: 'b', label: {'en': 'Test B'}, when: 'true'}
    ]
  };

  const newState = formReducer(testForm, action);
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs2');
    expect(vsIndex).toBeGreaterThan(-1);
    expect(newState.valueSets[vsIndex].entries).toBeDefined();
    expect(newState.valueSets[vsIndex].entries[0]).toStrictEqual({id: 'a', label: {'en': 'Test A'}});
    expect(newState.valueSets[vsIndex].entries[1]).toStrictEqual({id: 'b', label: {'en': 'Test B'}, when: 'true'});
  }
});

test('Add valueset entry: empty', () => {
  const action: ComposerAction = {
    type: 'addValueSetEntry',
    valueSetId: 'vs2'
  };
  const newState = formReducer(testForm, action);
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs2');
    expect(vsIndex).toBeGreaterThan(-1);
    expect(newState.valueSets[vsIndex].entries).toBeDefined();
    expect(newState.valueSets[vsIndex].entries.length).toEqual(7);
    expect(newState.valueSets[vsIndex].entries[6]).toStrictEqual({id: '', label: {}});
  }
});


test('Add valueset entry: filled', () => {
  const action: ComposerAction = {
    type: 'addValueSetEntry',
    valueSetId: 'vs2',
    entry: {
      id: 'a', label: {'en': 'Test A'}, when: 'true'
    }
  };
  const newState = formReducer(testForm, action);
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs2');
    expect(vsIndex).toBeGreaterThan(-1);
    expect(newState.valueSets[vsIndex].entries).toBeDefined();
    expect(newState.valueSets[vsIndex].entries.length).toEqual(7);
    expect(newState.valueSets[vsIndex].entries[6]).toStrictEqual({id: 'a', label: {'en': 'Test A'}, when: 'true'});
  }
});

test('Update valueset entry', () => {
  const action: ComposerAction = {
    type: 'updateValueSetEntry',
    valueSetId: 'vs2',
    index: 3,
    entry: {
      id: 'a', label: {'en': 'Test A'}, prop: 'test'
    }
  };
  const newState = formReducer(testForm, action);
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs2');
    expect(vsIndex).toBeGreaterThan(-1);
    expect(newState.valueSets[vsIndex].entries).toBeDefined();
    expect(newState.valueSets[vsIndex].entries.length).toEqual(6); 
    expect(newState.valueSets[vsIndex].entries[3]).toStrictEqual({id: 'a', label: {'en': 'Test A'}, prop: 'test'});
  }
});

test('Delete valueset entry', () => {
  const action: ComposerAction = {
    type: 'deleteValueSetentry',
    valueSetId: 'vs2',
    index: 3
  };
  const newState = formReducer(testForm, action);
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs2');
    expect(vsIndex).toBeGreaterThan(-1);
    expect(newState.valueSets[vsIndex].entries.length).toEqual(5);
    expect(newState.valueSets[vsIndex].entries[3].id).toEqual('prevention5');
  }
});

test('Move valueset entry', () => {
  const action: ComposerAction = {
    type: 'moveValueSetEntry',
    valueSetId: 'vs2',
    from: 3,
    to: 1
  };
  const newState = formReducer(testForm, action);
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs2');
    expect(vsIndex).toBeGreaterThan(-1);
    expect(newState.valueSets[vsIndex].entries.length).toEqual(6);
    expect(newState.valueSets[vsIndex].entries[1].id).toEqual('prevention4');
    expect(newState.valueSets[vsIndex].entries[3].id).toEqual('prevention3');
  }
});

test('Move valueset entry: past end', () => {
  const action: ComposerAction = {
    type: 'moveValueSetEntry',
    valueSetId: 'vs2',
    from: 3,
    to: 99
  };
  const newState = formReducer(testForm, action);
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs2');
    expect(vsIndex).toBeGreaterThan(-1);
    expect(newState.valueSets[vsIndex].entries.length).toEqual(6);
    expect(newState.valueSets[vsIndex].entries[3].id).toEqual('prevention5');
    expect(newState.valueSets[vsIndex].entries[5].id).toEqual('prevention4');
  }
});

test('Move valueset entry: first', () => {
  const action: ComposerAction = {
    type: 'moveValueSetEntry',
    valueSetId: 'vs2',
    from: 3,
    to: 0
  };
  const newState = formReducer(testForm, action);
  expect(newState.valueSets).toBeDefined();
  if (newState.valueSets) {
    const vsIndex = newState.valueSets.findIndex(vs => vs.id === 'vs2');
    expect(vsIndex).toBeGreaterThan(-1);
    expect(newState.valueSets[vsIndex].entries.length).toEqual(6);
    expect(newState.valueSets[vsIndex].entries[0].id).toEqual('prevention4');
    expect(newState.valueSets[vsIndex].entries[3].id).toEqual('prevention3');
  }
});

test('Set global valueset name', () => {
  const action: ComposerAction = {
    type: 'setGlobalValueSetName',
    valueSetId: 'vs45',
    name: 'test'
  };
  const newState = formReducer(testForm, action);
  expect(newState.metadata?.composer?.globalValueSets).toBeDefined();
  if (newState.metadata?.composer?.globalValueSets) {
    const gvsIndex = newState.metadata.composer.globalValueSets.findIndex(gvs => gvs.valueSetId === 'vs45');
    expect(gvsIndex).toBeGreaterThan(-1);
    expect(newState.metadata.composer.globalValueSets[gvsIndex].label).toEqual('test');
  }
});

test('Set form metadata value', () => {
  const action: ComposerAction = {
    type: 'setMetadataValue',
    attr: 'test',
    value: {
      some: {
        struct: 1
      }
    }
  };
  const newState = formReducer(testForm, action);
  expect(newState.metadata.test).toStrictEqual({
                                    some: {
                                      struct: 1
                                    }
                                  });
});

test('Create context variable', () => {
  const action: ComposerAction = {
    type: 'createVariable',
    context: true
  };
  const newState = formReducer(testForm, action);
  expect(newState.variables).toBeDefined();
  if (newState.variables) {
    const vIndex = newState.variables.findIndex(v => v.name === 'context1');
    expect(vIndex).toBeGreaterThan(-1);
    expect(newState.variables[vIndex]).toStrictEqual({name: 'context1', context: true, contextType: 'text'});
  }
});

test('Create expression variable', () => {
  const action: ComposerAction = {
    type: 'createVariable',
    context: false
  };
  const newState = formReducer(testForm, action);
  expect(newState.variables).toBeDefined();
  if (newState.variables) {
    const vIndex = newState.variables.findIndex(v => v.name === 'var1');
    expect(vIndex).toBeGreaterThan(-1);
    expect(newState.variables[vIndex]).toStrictEqual({name: 'var1', expression: ''});
  }
});

test('Create expression variable, clean form', () => {
  const action: ComposerAction = {
    type: 'createVariable',
    context: false
  };
  const newState = formReducer(cleanForm, action);
  expect(newState.variables).toBeDefined();
  if (newState.variables) {
    const vIndex = newState.variables.findIndex(v => v.name === 'var1');
    expect(vIndex).toBeGreaterThan(-1);
    expect(newState.variables[vIndex]).toStrictEqual({name: 'var1', expression: ''});
  }
});

test('Update context variable, default value', () => {
  const action: ComposerAction = {
    type: 'updateContextVariable',
    variableId: 'prefilledCompanyID',
    defaultValue: 'test'
  };
  const newState = formReducer(testForm, action);
  expect(newState.variables).toBeDefined();
  if (newState.variables) {
    const vIndex = newState.variables.findIndex(v => v.name === 'prefilledCompanyID');
    expect(vIndex).toBeGreaterThan(-1);
    expect(newState.variables[vIndex]).toStrictEqual({name: 'prefilledCompanyID', context: true, defaultValue: 'test', contextType: 'text'});
  }
});

test('Update context variable, update all attributes', () => {
  const action: ComposerAction = {
    type: 'updateContextVariable',
    variableId: 'prefilledCompanyID',
    defaultValue: 105,
    contextType: 'number'
  };
  const newState = formReducer(testForm, action);
  expect(newState.variables).toBeDefined();
  if (newState.variables) {
    const vIndex = newState.variables.findIndex(v => v.name === 'prefilledCompanyID');
    expect(vIndex).toBeGreaterThan(-1);
    expect(newState.variables[vIndex]).toStrictEqual({name: 'prefilledCompanyID', context: true, defaultValue: 105, contextType: 'number'});
  }
});

test('Update expression variable', () => {
  const action: ComposerAction = {
    type: 'updateExpressionVariable',
    variableId: 'companyMainBL',
    expression: '\'a\' = \'a\''
  }
  const newState = formReducer(testForm, action);
  expect(newState.variables).toBeDefined();
  if (newState.variables) {
    const vIndex = newState.variables.findIndex(v => v.name === 'companyMainBL');
    expect(vIndex).toBeGreaterThan(-1);
    expect(newState.variables[vIndex]).toStrictEqual({name: 'companyMainBL', expression: '\'a\' = \'a\''});
  }
});

test('Delete variable', () => {
  const action: ComposerAction = {
    type: 'deleteVariable',
    variableId: 'companyMainBL'
  };
  const newState = formReducer(testForm, action);
  expect(newState.variables).toBeDefined();
  if (newState.variables) {
    expect(newState.variables.length).toEqual(4);
    const vIndex = newState.variables.findIndex(v => v.name === 'companyMainBL');
    expect(vIndex).toEqual(-1);
  }
});

test('Add language, existing', () => {
  const action: ComposerAction = {
    type: 'addLanguage',
    language: 'fi'
  };
  const newState = formReducer(testForm, action);
  expect(newState.metadata.languages).toBeDefined();
  if (newState.metadata.languages) {
    expect(newState.metadata.languages.length).toEqual(2);
  }
});

test('Add language, no copy', () => {
  const action: ComposerAction = {
    type: 'addLanguage',
    language: 'et'
  };
  const newState = formReducer(testForm, action);
  expect(newState.metadata.languages).toBeDefined();
  if (newState.metadata.languages) {
    expect(newState.metadata.languages.length).toEqual(3);
    expect(newState.metadata.languages.indexOf('et')).toBeGreaterThan(-1);
  }
});

test('Add language, clean form', () => {
  const action: ComposerAction = {
    type: 'addLanguage',
    language: 'et'
  };
  const newState = formReducer(cleanForm, action);
  expect(newState.metadata.languages).toBeDefined();
  if (newState.metadata.languages) {
    expect(newState.metadata.languages.length).toEqual(1);
    expect(newState.metadata.languages.indexOf('et')).toBeGreaterThan(-1);
  }
});

test('Add language, copy', () => {
  const action: ComposerAction = {
    type: 'addLanguage',
    language: 'et',
    copyFrom: 'fi'
  };
  const newState = formReducer(testForm, action);
  expect(newState.metadata.languages).toBeDefined();
  if (newState.metadata.languages) {
    expect(newState.metadata.languages.length).toEqual(3);
    expect(newState.metadata.languages.indexOf('et')).toBeGreaterThan(-1);
  }
  expect(newState.data.list24.label?.et).toBeDefined();
  expect(newState.data.list24.label?.et).toEqual(newState.data.list24.label?.fi);

  expect(newState.data.list24.description?.et).toBeDefined();
  expect(newState.data.list24.description?.et).toEqual(newState.data.list24.description?.fi);

  const val = newState.data.workRelatedRiskAnalysis?.validations;
  expect(val).toBeDefined();
  if (val) {
    expect(val.length).toEqual(1);
    expect(val[0].message?.et).toBeDefined();
    expect(val[0].message?.et).toEqual(val[0].message?.fi);
  }

  expect(newState?.valueSets).toBeDefined(); 
  if (newState.valueSets) {
    const vs = newState.valueSets[0];
    expect(vs.entries[0].label?.et).toBeDefined();
    expect(vs.entries[0].label?.et).toEqual(vs.entries[0].label?.fi);
  }  
});

test('Delete language', () => {
  const action: ComposerAction = {
    type: 'deleteLanguage',
    language: 'en'
  };
  const newState = formReducer(testForm, action);
    
  expect(newState.metadata.languages).toBeDefined();
  if (newState.metadata.languages) {
    expect(newState.metadata.languages.length).toEqual(1);
    expect(newState.metadata.languages.indexOf('en')).toEqual(-1);
  }

  expect(newState.data.list24.label?.en).not.toBeDefined();
  expect(newState.data.list24.description?.en).not.toBeDefined();

  const val = newState.data.workRelatedRiskAnalysis?.validations;
  expect(val).toBeDefined();
  if (val) {
    expect(val[0].message?.en).toBeUndefined();
  }

  expect(newState?.valueSets).toBeDefined(); 
  if (newState.valueSets) {
    const vsIdx = newState.valueSets.findIndex(vs => vs.id === 'vs45');
    expect(vsIdx).toBeGreaterThan(-1);
    expect(newState.valueSets[vsIdx].entries[0].label.en).toBeUndefined();
    expect(newState.valueSets[vsIdx].entries[0].label.en).toBeUndefined();
  }  
});
