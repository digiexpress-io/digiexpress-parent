import React from 'react';
import {treeItemFactory, TreeItem} from '../../src/items';
import Immutable from 'immutable';
import {mount, render, shallow} from 'enzyme'
import sinon from 'sinon';
import Adapter from 'enzyme-adapter-react-16';
import Enzyme from 'enzyme';

Enzyme.configure({ adapter: new Adapter() });

const TestComponentA = (props) => <div>A</div>;
const TestComponentB = (props) => <div>B</div>;
const TestComponentD= (props) => <div>D</div>;
const TestFixture = ({item, props, config}) => <div>{treeItemFactory(item, props, config)}</div>;

const TEST_CONFIG = {
  items: [
    {
      matcher: item => item.get('type') === 'a',
      component: TestComponentA,
      props: {
        icon: 'square outline',
        placeholder: 'Group label'
      }
    },
    {
      matcher: item => item.get('type') === 'b',
      component: TestComponentB,
      props: {
        icon: 'font',
        placeholder: 'Text field label'
      }
    },
    {
      matcher: item => item.get('type') === 'd',
      component: TestComponentD,
      hideInTree: true,
      props: {
        icon: 'font',
        placeholder: 'Text field label'
      }
    }
  ]
};

describe('treeItemFactory', () => {
  it ('Creates component A', () => {
   const wrapper = shallow(<TestFixture item={Immutable.fromJS({type: 'a'})} config={TEST_CONFIG}/>);
   expect(wrapper.containsMatchingElement(<TreeItem key='a' item={Immutable.fromJS({type: 'a'})} />)).to.equal(true);
  });

  it ('Creates component B', () => {
    const wrapper = shallow(<TestFixture item={Immutable.fromJS({type: 'b'})} config={TEST_CONFIG}/>);
    expect(wrapper.containsMatchingElement(<TreeItem key='a' item={Immutable.fromJS({type: 'b'})} />)).to.equal(true);
  });

  it ('Creates component A with custom props', () => {
    const wrapper = shallow(<TestFixture item={Immutable.fromJS({type: 'a'})} config={TEST_CONFIG} props={{d: 'test'}}/>);
    expect(wrapper.containsMatchingElement(<TreeItem d='test' />)).to.equal(true);
  });

  it ('Doesn\'t create tree item if hidden', () => {
    const wrapper = shallow(<TestFixture item={Immutable.fromJS({type: 'd'})} config={TEST_CONFIG}/>);
    expect(wrapper.children().exists()).to.equal(false);
  });

  it ('Skip unknown components', () => {
    const wrapper = shallow(<TestFixture item={Immutable.fromJS({type: 'c'})} config={TEST_CONFIG} />);
    expect(wrapper.children().exists()).to.equal(false);
  });

  it ('Tolerates null input', () => {
    const wrapper = shallow(<TestFixture config={TEST_CONFIG} />);
    expect(wrapper.children().exists()).to.equal(false);
  });

});
