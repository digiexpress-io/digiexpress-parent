import React, {Component} from 'react';
import {Table, Button, Input, Message, Ref, Segment, Icon} from 'semantic-ui-react';
import {connect} from 'react-redux';
import {findValueset} from '../helpers/utils';
import {createValueset, createValuesetEntry, updateValuesetEntry, deleteValuesetEntry, moveValuesetEntry, updateValueSetEntryAttr, setValuesetEntries} from '../actions';
import * as Defaults from '../defaults';
import { translateErrorMessage } from '../helpers/utils';
import { DragSource, DropTarget } from 'react-dnd'
import {findDOMNode} from 'react-dom';
import { DEFAULT_VALUESET_PROPS } from '../defaults';
import PopupText from './PopupText';
import {ValuesetUploadDialog} from './ValuesetUploadDialog';
import FileSaver from 'file-saver';
import Papa from 'papaparse';
import {CodeEditor} from './CodeEditor';
import Immutable from 'immutable';

const VisibilityEditor = ({attached, valueSetId, entry, updateValueSetEntryAttr, valueSetErrors, index}) => {
 const  entryErrors = entry ? valueSetErrors && valueSetErrors.filter(e => e.get('index') === index && e.get('tyoe') === 'VALUESET_ENTRY') :  new Immutable.List([]);
 return (
    <Segment attached={attached}>
      {!entry ? <i>Please select entry...</i> :
      <div>
      <label>Visibility rule for <i>{entry.get('id')}</i>:</label>
      <CodeEditor active={true} value={entry.get('when') || ''} onChange={value => updateValueSetEntryAttr(valueSetId, entry.get('id'), 'when', value)} placeholder='Visibility' errors={entryErrors}/>
      </div>
      }
    </Segment>
  );
};

const DropPosition = {
  ABOVE: 0,
  BELOW: 1
};

const getDropPosition = (boundingRect, clientOffset) => {
  const midY = (boundingRect.bottom - boundingRect.top) / 2;
  const y = clientOffset.y - boundingRect.top;
  if (y >= midY) {
    return DropPosition.BELOW;
  } else {
    return DropPosition.ABOVE;
  }
}

const rowSource = {
  beginDrag(props) {
    return {
      index: props.index
    };
  }
};

const rowTarget = {
  drop(props, monitor, component) {
    const dragIndex = monitor.getItem().index;
    const hoverIndex = props.index;
    if (dragIndex === hoverIndex) { return; }
    if (monitor.didDrop()) { return; }
    const dropPosition = getDropPosition(findDOMNode(component).getBoundingClientRect(), monitor.getClientOffset());

    const targetIndex = dropPosition === DropPosition.BELOW ? hoverIndex + 1 : hoverIndex;

    if (dragIndex === targetIndex) { return; }
    props.moveEntry(dragIndex, targetIndex);
  }
}

class EntryRow extends Component {

  constructor(props) {
    super(props);
    this.node = null;
  }

  render() {
    const {entry, index, valueSetErrors, deleteValuesetEntry, updateValuesetEntry, valueSetId, language, isOver, connectDragSource, connectDropTarget, clientOffset, valueSetPropEditors, updateValueSetEntryAttr, active, setActiveItem} = this.props;
    const entryErrors = valueSetErrors && valueSetErrors.find(e => e.get('index') === index || e.get('expression') === entry.get('id'));
    let dragClass = null;
    if (isOver) {
      const dropPosition = getDropPosition(this.node.getBoundingClientRect(), clientOffset);
      dragClass = dropPosition === DropPosition.ABOVE ? 'composer-drag-above' : 'composer-drag-below';
    }
    return (
      <Ref innerRef={node => {connectDropTarget(node); connectDragSource(node); this.node = node;}}>
        <Table.Row key={index} error={entryErrors ? true : false} className={dragClass} active={active} onClick={() => setActiveItem(index)}>
          <Table.Cell collapsing textAlign='center'>
            <Button size='tiny' icon='remove' onClick={() => deleteValuesetEntry(valueSetId, index)} />
            <Icon name='eye' disabled={!entry.get('when')}/>
          </Table.Cell>
          <Table.Cell >
              <Input transparent fluid value={entry.get('id') || ''} onChange={(e) => updateValuesetEntry(valueSetId, index, e.target.value, null, null)} />
          </Table.Cell>
          <Table.Cell>
              <PopupText value={entry.getIn(['label', language]) || ''} onChange={(v) => updateValuesetEntry(valueSetId, index, null, v, language)} />
          </Table.Cell>
          {
            valueSetPropEditors.map((e, i) =>
            <Table.Cell key={i} >
                <e.editor value={entry.get(e.name)} onChange={(value) => updateValueSetEntryAttr(valueSetId, entry.get('id'), e.name, value)} />
            </Table.Cell>)
          }
        </Table.Row>
      </Ref>);
  }
};

const DraggableEntryRow =
  DragSource('vsrow', rowSource, (connect, monitor) => ({
  connectDragSource: connect.dragSource(),
  isDragging: monitor.isDragging()
}))(DropTarget('vsrow', rowTarget, (connect, monitor) => ({
  connectDropTarget: connect.dropTarget(),
  isOver: monitor.isOver({shallow: true}),
  clientOffset: monitor.getClientOffset()
}))(EntryRow))

class ValueSetEditor extends Component {

  constructor(props) {
    super(props);
    this.moveEntry = this.moveVSEntry.bind(this);
    this.state = {
      activeItem: null
    };
  }

  moveVSEntry(from, to) {
    this.props.moveValuesetEntry(this.props.valueSetId, from, to);
  }

  getValueSetPropEditors() {
    const config = this.props.valueSetProps || DEFAULT_VALUESET_PROPS;
    return config ? config : [];
  }

  downloadValueSet(valueSet) {
    const entries = valueSet.get('entries').toJS();
    const result = [];
    entries.forEach(e => {
      let entry = {ID: e.id};
      for (const lang in e.label) {
        entry[lang] = e.label[lang];
      }
      result.push(entry);
    });
    const csv = Papa.unparse(result);
    const blob = new Blob([csv], {type: 'text/csv'});
    FileSaver.saveAs(blob, `valueSet.csv`);
  }

  render() {
      const {deleteValuesetEntry, updateValuesetEntry, valueSetId, language, updateValueSetEntryAttr, setValuesetEntries} = this.props;
      const dedupe = (item, idx, arr) => arr.indexOf(item) === idx;
      const valueSetPropEditors = this.getValueSetPropEditors();
      const valueSetErrors = this.props.errors &&
          this.props.errors
            .filter(e => {
              const [vsId] = e.get('itemId').split(':', 2);
              return e.get('type').startsWith('VALUESET') && vsId === valueSetId}
            );
      const errors = valueSetErrors && valueSetErrors.groupBy(e => e.get('level'));

      const errorList = errors && errors.get('ERROR') &&
                <Message attached={errors.get('WARNING') ? true : 'bottom'} error header='Errors'
                    list={errors.get('ERROR').map(e => translateErrorMessage(e)).toJS().filter(dedupe)} />;

      const warningList = errors && errors.get('WARNING') &&
                <Message attached='bottom' warning header='Warnings'
                    list={errors.get('WARNING').map(e => translateErrorMessage(e)).toJS().filter(dedupe)} />;

      const setActiveItem = (activeItemIndex) => {
        this.setState({activeItem: activeItemIndex});
      }

      const rows = this.props.getValueset().get('entries')
        ? this.props.getValueset().get('entries').map((e, i) =>
           <DraggableEntryRow key={i} entry={e} moveEntry={this.moveEntry} index={i} valueSetErrors={valueSetErrors}
              deleteValuesetEntry={deleteValuesetEntry} updateValuesetEntry={updateValuesetEntry} valueSetId={valueSetId} language={language}
              valueSetPropEditors={valueSetPropEditors}  updateValueSetEntryAttr={updateValueSetEntryAttr} active={this.state.activeItem === i} setActiveItem={setActiveItem}/>)
        : [];

      const activeEntry = this.props.getValueset().get('entries') && this.props.getValueset().get('entries').get(this.state.activeItem);

      return (
      <React.Fragment>
        <Table celled attached='top'>
          <colgroup>
            <col width={1} />
          </colgroup>
          <colgroup span={3} width={`${100 / (valueSetPropEditors.length + 2)}%`}  />
          <Table.Header>
            <Table.Row>
              <Table.HeaderCell collapsing>
                <Button size='tiny' icon='add' onClick={() => this.props.createValuesetEntry(this.props.getValueset().get('id'))} />
                <ValuesetUploadDialog entries={this.props.getValueset().get('entries')} setEntries={entries => {
                  setValuesetEntries(valueSetId, entries);
                }} />
                <Button size='tiny' icon='download' onClick={() => this.downloadValueSet(this.props.getValueset())}/>
              </Table.HeaderCell>
              <Table.HeaderCell>Key</Table.HeaderCell>
              <Table.HeaderCell>Text</Table.HeaderCell>
              {
                valueSetPropEditors.map((e, i) => <Table.HeaderCell key={i}>{e.title}</Table.HeaderCell>)
              }
            </Table.Row>
          </Table.Header>
          <Table.Body>
            {rows}
          </Table.Body>
        </Table>
        <VisibilityEditor attached={errors ? true : 'bottom'} valueSetId={valueSetId} entry={activeEntry} updateValueSetEntryAttr={updateValueSetEntryAttr} valueSetErrors={valueSetErrors} index={this.state.activeItem}/>
        {errorList}
        {warningList}
      </React.Fragment>
      );
  }
}

const ValueSetEditorConnected = connect(
  (state, props) => ({
    language: (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE,
    get getValueset() { return () => findValueset(state.dialobComposer.form, props.valueSetId); },
    errors: state.dialobComposer.editor && state.dialobComposer.editor.get('errors'),
    valueSetProps:  state.dialobComposer.config && state.dialobComposer.config.valueSetProps
  }), {
    createValueset,
    createValuesetEntry,
    updateValuesetEntry,
    deleteValuesetEntry,
    moveValuesetEntry,
    updateValueSetEntryAttr,
    setValuesetEntries
  }
)(ValueSetEditor);

export {
  ValueSetEditorConnected as default,
  ValueSetEditor
};
