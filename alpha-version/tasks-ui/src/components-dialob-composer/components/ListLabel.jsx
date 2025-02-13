import React from 'react';
import {Table, Icon, Label} from 'semantic-ui-react';
import {CHOICE_ITEM_TYPES} from '../defaults';
import { isGlobalValueSet } from '../helpers/utils';


export const ListLabel = ({item,globalValueSets}) => {

  return (
    <>
      {
        (CHOICE_ITEM_TYPES.findIndex(t => t === item.get('type')) > -1) && (
          <Table.Cell style={{display: 'flex', justifyContent: 'end'}}>
            {
              item.get('valueSetId') ? (
                isGlobalValueSet(globalValueSets, item.get('valueSetId')) ? 
                <Label as='p'>
                    <Icon name='checkmark' styleClass='labeled-icon' />
                  Global list:&nbsp; {globalValueSets.toJS().find(vs => vs.valueSetId === item.get('valueSetId'))?.label }
                  
                </Label> : 
                <Label as='p'>
                  <Icon name='checkmark' styleClass='labeled-icon' />
                  List  
                </Label> 
              ) :
              <Label as='p'>
                <Icon name='warning sign' styleClass='labeled-icon' color='yellow' />
                No list
              </Label>
    
            }
          </Table.Cell>
      )
      }
    </>
  );
}