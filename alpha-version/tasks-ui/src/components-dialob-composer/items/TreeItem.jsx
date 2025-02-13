import React from 'react';
import {List, Ref} from 'semantic-ui-react';
import Item, {connectItem} from './Item';
import {treeItemFactory} from '.';
import md_strip_tags from 'remove-markdown';
import classnames from 'classnames';
import { DragSource, DropTarget } from 'react-dnd'
import {findDOMNode} from 'react-dom';
import {canContain} from '../defaults';
import memoize from 'memoizee';
import * as Status from '../helpers/constants';

const MAX_LENGTH = 55;

const DropPosition = {
  ABOVE: 0,
  BELOW: 1,
  INSIDE: 2
};

const formatLabel = memoize((label, type) => {
  if (!label) {
     return label;
  }
  const text = type === 'note' ? md_strip_tags(label) : label;
  return text.length > MAX_LENGTH ? text.substring(0, MAX_LENGTH) + '\u2026': text;
});

const getDropPosition = (boundingRect, clientOffset, parentType, targetType, itemType) => {
  let result = null;
  const canDropAside = canContain(parentType, itemType);
  const canDropIn = canContain(targetType, itemType)
  if (canDropAside) {
    const midY = (boundingRect.bottom - boundingRect.top) / 2;
    const y = clientOffset.y - boundingRect.top;

    if (y >= midY) {
      result = DropPosition.BELOW;
    } else {
      result = DropPosition.ABOVE;
    }
  }

  if (canDropIn && canDropAside) {
    const x = clientOffset.x - boundingRect.left;
    const midX = (boundingRect.right - boundingRect.left) / 2;
    if (x >= midX) {
      result = DropPosition.INSIDE;
    }
  }

  if (canDropIn && !canDropAside) {
    result = DropPosition.INSIDE;
  }

  return result;
}

const itemSource = {
  beginDrag(props) {
    return {
      id: props.id,
      index: props.index,
      parent: props.parent,
      isPage: props.isPage,
      itemType: props.itemType
    }
  }
};

const itemTarget = {

  drop(props, monitor, component) {
    const dragIndex = monitor.getItem().index;
    const dragParent = monitor.getItem().parent;
    const hoverIndex = props.index;
    const hoverParent = props.parent;
    if (dragIndex === hoverIndex
        && dragParent.get('id') === hoverParent.get('id')
    ) {
      return;
    }
    if (monitor.didDrop()) {
      return;
    }

    const dropPosition = getDropPosition(findDOMNode(component).getBoundingClientRect(), monitor.getClientOffset(), props.parent.get('type'), props.isPage ? 'page' : props.itemType, monitor.getItem().isPage ? 'page' : monitor.getItem().itemType);
    if (dropPosition === DropPosition.ABOVE) {
      props.moveItem(dragIndex, hoverIndex, dragParent.get('id'), hoverParent.get('id'), monitor.getItem().id);
    } else if (dropPosition === DropPosition.BELOW) {
      props.moveItem(dragIndex, hoverIndex + 1, dragParent.get('id'), hoverParent.get('id'), monitor.getItem().id);
    } else if (dropPosition === DropPosition.INSIDE) {
      props.moveItem(dragIndex, 0, dragParent.get('id'), props.item.get('id'), monitor.getItem().id);
    }
  }
};

class TreeItem extends Item {

  constructor(props) {
    super(props);
    this.node = null;
  }

  createChildren(props, config) {
    return this.props.item.get('items') && this.props.item.get('items')
      .map(itemId => this.props.getItemById(itemId))
      .map((item, index) => treeItemFactory(item, Object.assign(props, {index}), config));
  }

  getSubList() {
    const parent = this.props.item;
    const children = this.createChildren({pageId: this.props.pageId, parent, moveItem: this.props.moveItem, isPage: false, getItemById: this.props.getItemById}, this.props.itemEditors);
    if (children && children.size > 0) {
      return (
        <List.List>
          {children}
        </List.List>
      )
    }
    return null;
  }

  getLabel() {
    const text= formatLabel(this.props.item.getIn(['label', this.props.language]), this.props.item.get('type'));
    return !text ? <em>{this.props.itemId}</em> : text;
  }

  render() {
    const {connectDragSource, connectDropTarget, isOver, clientOffset, treeCollapsed, isPage} = this.props;
    let dragClass = null;
    if (isOver) {
      const dropPosition = getDropPosition(this.node.getBoundingClientRect(), clientOffset, this.props.parent.get('type'),  isPage ? 'page': this.props.itemType, this.props.dragItem.isPage ? 'page' : this.props.dragItem.itemType);
      if (dropPosition === DropPosition.ABOVE) {
        dragClass = 'composer-drag-above';
      } else if (dropPosition === DropPosition.BELOW) {
        dragClass = 'composer-drag-below';
      } else if (dropPosition === DropPosition.INSIDE) {
        dragClass = 'composer-drag-inside';
      }

    }
    const errorLevel = this.getErrorLevel();
    return (
      <Ref innerRef={node => {connectDropTarget(node); connectDragSource(node); this.node = node;}}>
        <List.Item className={dragClass}>
          {
            this.props.treeCollapsible &&
              <List.Icon name={treeCollapsed ? 'caret right' : 'caret down'} style={{float: 'initial'}} onClick={() => this.setTreeCollapsed(!treeCollapsed)}/>
          }
          <List.Icon name={errorLevel === Status.STATUS_OK ? this.props.icon : 'warning sign'}
             color={ errorLevel === Status.STATUS_ERRORS ? 'red' : errorLevel === Status.STATUS_WARNINGS ? 'yellow' : 'black'}
             style={{float: 'initial'}}/>
          <List.Content>
            <List.Header onClick={() => this.setActive()}
              className={classnames({'composer-active': this.props.active,
                'composer-warning': errorLevel === Status.STATUS_WARNINGS,
                'composer-error': errorLevel === Status.STATUS_ERRORS
            })}
            >
              {this.getLabel()}
            </List.Header>
            {!treeCollapsed && this.getSubList()}
          </List.Content>
        </List.Item>
      </Ref>
    );
  }
}

const TreeItemConnected =
  connectItem(DragSource('item', itemSource, (connect, monitor) =>
    ({
      connectDragSource: connect.dragSource(),
      isDragging: monitor.isDragging
    }))
    (DropTarget('item', itemTarget, (connect, monitor) => ({
      connectDropTarget: connect.dropTarget(),
      isOver: monitor.isOver({shallow: true}),
      clientOffset: monitor.getClientOffset(),
      dragItem: monitor.getItem()
    }))(TreeItem)));

export {
  TreeItem,
  TreeItemConnected as default
};
