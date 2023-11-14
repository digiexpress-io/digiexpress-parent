import React from 'react';
import {itemFactory} from '../../src/items';
import Immutable from 'immutable';
import {shallow} from 'enzyme'
import Adapter from 'enzyme-adapter-react-16';
import Enzyme from 'enzyme';

Enzyme.configure({ adapter: new Adapter() });

const TestComponentA = (props) => <div>A</div>;
const TestComponentB = (props) => <div>B</div>;
const TestFixture = ({item, props, config}) => <div>{itemFactory(item, props, config)}</div>;

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
    }
  ]
};

describe('itemFactory', () => {
  it ('Creates component A', () => {
   const wrapper = shallow(<TestFixture item={Immutable.fromJS({type: 'a'})} config={TEST_CONFIG}/>);
   expect(wrapper.containsMatchingElement(<TestComponentA key='a' item={Immutable.fromJS({type: 'a'})} />)).to.equal(true);
  });

  it ('Creates component B', () => {
    const wrapper = shallow(<TestFixture item={Immutable.fromJS({type: 'b'})} config={TEST_CONFIG}/>);
    expect(wrapper.containsMatchingElement(<TestComponentB />)).to.equal(true);
  });

  it ('Creates component A with custom props', () => {
    const wrapper = shallow(<TestFixture item={Immutable.fromJS({type: 'a'})} config={TEST_CONFIG} props={{d: 'test'}}/>);
    expect(wrapper.containsMatchingElement(<TestComponentA d='test' />)).to.equal(true);
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
