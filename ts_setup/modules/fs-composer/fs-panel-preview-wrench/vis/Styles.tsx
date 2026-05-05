import { styled } from '@mui/material';
import '@xyflow/react/dist/style.css';


function shape(type: string, props: {
  borderRadius: string,
  height: string,
  width: string,
  transform?: string,
}) {

  const result: Record<string, any> = {};
  result[`.react-flow__node-${type}.selectable.selected`] = {
      ':before': {
        transform: props.transform,
        borderRadius: props.borderRadius,
        border: '3px solid rgb(245, 125, 189)',
        boxShadow: '0px 3.54px 4.55px 0px #00000005, 0px 3.54px 4.55px 0px #0000000D, 0px .51px 1.01px 0px #0000001A'
      }
  };

  result[`.react-flow__node-${type}`] = {
    height: props.height,
    width: props.width,
    ':before': {
      borderRadius: props.borderRadius,
      position: 'absolute',
      content: '""',
      top: '0px',
      left: '0px',
      height: '100%',
      width: '100%',
      boxShadow: '0px 0px 12px gray',
      transform: props.transform
    },
    ':after': {
      borderRadius: props.borderRadius,
      position: 'absolute',
      top: '10px',
      left: '10px',
      content: "''",
      height: 'calc(100% - 22px)',
      width: 'calc(100% - 22px)',
      border: '1px solid orange',
      transform: props.transform
    }
  };
  return result;
}


export const Styles = styled('div')(({ theme }) => ({
  height: '100%',
  width: '100%',

  '.react-flow__node': {
    boxShadow: 'unset',
    display: 'flex',
    flexDirection: 'column',
    fontFamily: 'monospace',
    justifyContent: 'center',
    alignItems: 'center',
    textAlign: 'center',
    fontSize: '15px',
    color: 'rgb(0, 0, 0)',
  },
  ...shape('start', {borderRadius: '50%', height: '100px', width: '100px'}),
  ...shape('end', {borderRadius: '50%', height: '100px', width: '100px'}),
  ...shape('service', {borderRadius: '10px', height: '100px', width: '200px'}),
  ...shape('decisionTable', {borderRadius: '10px', height: '100px', width: '200px', transform: 'skew(-25deg)'}),
  ...shape('switch', {borderRadius: '10px', height: '200px', width: '200px', transform: 'rotateX(45deg) rotateZ(45deg)'}),
}));
