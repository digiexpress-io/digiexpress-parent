import { formReducer } from "../../src/reducers/formReducer";
import { addItem, updateItem, deleteItem, createValueset, createValuesetEntry, updateValuesetEntry, deleteValuesetEntry, createContextVariable, createExpressionVariable, addLanguage, deleteLanguage } from '../../src/actions';
import Immutable from "immutable";

const INITAL_STATE = Immutable.fromJS({
  _id: "e8fa5f95873b08b1013a5f1fdd16be41",
  _rev: "5-044c0e3931d84d4a67de422da46aa57a",
  data: {
    questionnaire: {
      id: "questionnaire",
      type: "questionnaire",
      label: { en: "New Dialog" },
      items: ["page1"]
    },
    page1: {
      id: "page1",
      type: "group",
      label: { en: "New Page" },
      items: ["group1", "group2", "group4"]
    },
    group1: {
      id: "group1",
      type: "group",
      label: { en: "New Group" },
      items: ["text1", "text2"]
    },
    group2: {
      id: "group2",
      type: "group",
      label: { en: "New Group" },
      items: ["text3", "text4", "group3"]
    },
    group3: {
      id: "group3",
      type: "group",
      label: { en: "New Group" },
      items: ["text5"]
    },
    group4: {
      id: "group4",
      type: "group",
      label: { en: "New Group" }
    },
    text1: { id: "text1", type: "text", label: { en: "Text" } },
    text2: { id: "text2", type: "text", label: { en: "Text" } },
    text3: { id: "text3", type: "text", label: { en: "Text" } },
    text4: { id: "text4", type: "text", label: { en: "Text" } },
    text5: { id: "text5", type: "text", label: { en: "Text" } }
  },
  variables: [
    {
      "name": "text6",
      "defaultValue": "x",
      "context": true,
      "contextType": "text"
    }
  ],
  metadata: {
    label: "Simple",
    created: "2018-04-10T08:58:40.585+0000",
    lastSaved: "2018-04-10T08:58:56.438+0000",
    valid: true,
    creator: "xxx",
    tenantId: "itest",
    savedBy: "xxx",
    languages: ["en"]
  }
});

describe("formReducer", () => {
  it ('Adds new item into the end of the group', () => {
    const state = formReducer(INITAL_STATE, addItem({type: 'text'}, 'group3'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.toJS().data.text7).to.deep.equal({id: 'text7', type: 'text'});
    expect(state.toJS().data.group3.items).to.deep.equal(['text5', 'text7']);
  });
  it ('Adds new item into group that doesn\'t have any items', () => {
    const state = formReducer(INITAL_STATE, addItem({type: 'text'}, 'group4'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.toJS().data.text7).to.deep.equal({id: 'text7', type: 'text'});
    expect(state.toJS().data.group4.items).to.deep.equal(['text7']);
  });
  it ('Adds new item into the group after given item', () => {
    const state = formReducer(INITAL_STATE, addItem({type: 'text'}, 'group2', 'text4'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.toJS().data.text7).to.deep.equal({id: 'text7', type: 'text'});
    expect(state.toJS().data.group2.items).to.deep.equal(['text3', 'text4', 'text7', 'group3']);
  });
  it ('Updates normal attribute of an item', () => {
    const state = formReducer(INITAL_STATE, updateItem('text5', 'activeWhen', 'true'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.toJS().data.text5.activeWhen).to.equal('true');
  });
  it ('Updates translated attribute of an item', () => {
    const state = formReducer(INITAL_STATE, updateItem('text5', 'label', 'meh', 'fi'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.toJS().data.text5.label).to.deep.equal({'en': 'Text', 'fi': 'meh'});
  });
  it ('Removes item and all descendants', () => {
    let state = formReducer(INITAL_STATE, createValueset('text3'));
    state = formReducer(state, deleteItem('group2'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    // Descendants
    expect(state.toJS().data).to.not.have.property('group2');
    expect(state.toJS().data).to.not.have.property('group3');
    expect(state.toJS().data).to.not.have.property('text3');
    expect(state.toJS().data).to.not.have.property('text4');
    expect(state.toJS().data).to.not.have.property('text5');

    // Reference
    expect(state.toJS().data.page1.items).to.deep.equal(['group1', 'group4']);

    // Valueset
    expect(state.toJS().valueSets).not.to.deep.contain({id: 'vs1', entries: []});

    // Survivors
    expect(state.toJS().data).to.have.property('questionnaire');
    expect(state.toJS().data).to.have.property('page1');
    expect(state.toJS().data).to.have.property('group1');
    expect(state.toJS().data).to.have.property('group4');
    expect(state.toJS().data).to.have.property('text1');
    expect(state.toJS().data).to.have.property('text2');
  });
  it ('Creates valueset structure', () => {
    const state = formReducer(INITAL_STATE, createValueset());
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.get('valueSets')).to.be.an.instanceof(Immutable.List);
    expect(state.toJS().valueSets).to.deep.include({id: 'vs1', entries: []});
  });
  it ('Create valueset to existing structure', () => {
    let state = formReducer(INITAL_STATE, createValueset());
    state = formReducer(state, createValueset());
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.get('valueSets')).to.be.an.instanceof(Immutable.List);
    expect(state.toJS().valueSets).to.deep.include({id: 'vs1', entries: []});
    expect(state.toJS().valueSets).to.deep.include({id: 'vs2', entries: []});
  });
  it ('Creates valueset for an item', () => {
    const state = formReducer(INITAL_STATE, createValueset('text1'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.get('valueSets')).to.be.an.instanceof(Immutable.List);
    expect(state.toJS().valueSets).to.deep.include({id: 'vs1', entries: []});
    expect(state.toJS().data.text1.valueSetId).to.equal('vs1');
  });
  it ('Adds entry to valueset', () => {
    let state = formReducer(INITAL_STATE, createValueset('text1'));
    state = formReducer(state, createValuesetEntry('vs1'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.get('valueSets')).to.be.an.instanceof(Immutable.List);
    expect(state.toJS().valueSets).to.deep.include({id: 'vs1', entries: [{id: '', label: {}}]});
  });
  it ('Set valueset entry ID', () => {
    let state = formReducer(INITAL_STATE, createValueset('text1'));
    state = formReducer(state, createValuesetEntry('vs1'));
    state = formReducer(state, updateValuesetEntry('vs1', 0, 'abc', null, null));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.get('valueSets')).to.be.an.instanceof(Immutable.List);
    expect(state.toJS().valueSets).to.deep.include({id: 'vs1', entries: [{id: 'abc', label: {}}]});
  });
  it ('Set valueset entry label', () => {
    let state = formReducer(INITAL_STATE, createValueset('text1'));
    state = formReducer(state, createValuesetEntry('vs1'));
    state = formReducer(state, updateValuesetEntry('vs1', 0, null, 'abc', 'en'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.get('valueSets')).to.be.an.instanceof(Immutable.List);
    expect(state.toJS().valueSets).to.deep.include({id: 'vs1', entries: [{id: '', label: {en: 'abc'}}]});
  });
  it ('Delete valueset entry', () => {
    let state = formReducer(INITAL_STATE, createValueset('text1'));
    state = formReducer(state, createValuesetEntry('vs1'));
    state = formReducer(state, deleteValuesetEntry('vs1', 0));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.get('valueSets')).to.be.an.instanceof(Immutable.List);
    expect(state.toJS().valueSets).to.deep.include({id: 'vs1', entries: []});
  });
  it ('Creates new context variable', () => {
    let state = formReducer(INITAL_STATE, createContextVariable());
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.get('variables')).to.be.an.instanceof(Immutable.List);
    expect(state.toJS().variables).to.deep.include({name: 'context1', context: true, contextType: 'text'});
  });
  it ('Creates new expression variable', () => {
    let state = formReducer(INITAL_STATE, createExpressionVariable());
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.get('variables')).to.be.an.instanceof(Immutable.List);
    expect(state.toJS().variables).to.deep.include({name: 'var1', expression: ''});
  });
  it ('Copies new language from existing', () => {
    let state = formReducer(INITAL_STATE, createValueset('text1'));
    state = formReducer(state, createValuesetEntry('vs1'));
    state = formReducer(state, updateValuesetEntry('vs1', 0, null, 'abc', 'en'));
    state = formReducer(state, addLanguage('fi', 'en'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.get('valueSets')).to.be.an.instanceof(Immutable.List);
    expect(state.toJS().valueSets).to.deep.include({id: 'vs1', entries: [{id: '', label: {en: 'abc', fi: 'abc'}}]});
    expect(state.toJS().data.text1.label).to.deep.equal({en: 'Text', fi: 'Text'});
    expect(state.toJS().metadata.languages).to.deep.equal(['en', 'fi']);
  });
  it ('Deletes a language', () => {
    let state = formReducer(INITAL_STATE, createValueset('text1'));
    state = formReducer(state, createValuesetEntry('vs1'));
    state = formReducer(state, updateValuesetEntry('vs1', 0, null, 'abc', 'en'));
    state = formReducer(state, addLanguage('fi', 'en'));
    state = formReducer(state, deleteLanguage('en'));
    expect(state).to.be.an.instanceof(Immutable.Map);
    expect(state.toJS().valueSets).to.deep.include({id: 'vs1', entries: [{id: '', label: {fi: 'abc'}}]});
    expect(state.toJS().data.text1.label).to.deep.equal({fi: 'Text'});
    expect(state.toJS().metadata.languages).to.deep.equal(['fi']);
  });
});

