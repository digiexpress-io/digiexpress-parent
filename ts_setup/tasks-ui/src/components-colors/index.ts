
// https://coolors.co/ff595e-26c485-ffca3a-1982c4-6a4c93
const bittersweet: string = '#FF595E'; //red
const sunglow: string = '#FFCA3A';     //yellow
const ultraviolet: string = '#6A4C93'; //lillac

const emerald: string = '#26C485';     //green
const steelblue: string = '#1982C4';   //blue
const orange: string = '#fb8500';      //orange
const red: string = '#d00000';
const cyan: string = 'rgb(80, 72, 229)';   //blue
const blue_mud = 'rgba(96, 113, 150, 0.5)' //blue
const blue = '#0088FE' //blue
const blue_mud2 = '#E9E9EA'; // blue
const white_mud = '#F5F5F5'; //white


const aquamarine = '#00C49F'; //green
const saffron = '#FFBB28'; //yellow
const mandarin = '#FF8042' //red
const moss = '#A1A314'; //green

const git_red = 'rgb(254, 237, 240)';
const git_green = 'rgb(230, 255, 237)';
const purple = 'rgba(80, 72, 229, 0.9)';
const grey = '#39393D';


const PALETTE = { red: bittersweet, green: emerald, yellow: sunglow, blue: steelblue, violet: ultraviolet };
const PALETTE_COLORS = Object.values(PALETTE);;
function withColors<T>(input: T[]): { color: string, value: T }[] {
  const result: { color: string, value: T }[] = [];
  
  let index = 0;
  for (const value of input) {
    result.push({ value, color: PALETTE_COLORS[index] })
    if (PALETTE_COLORS.length - 1 === index) {
      index = 0;
    } else {
      index++;
    }
  }

  return result;
}



export {
  withColors,
  ultraviolet,
  bittersweet,
  sunglow,
  grey,
  blue_mud2,
  purple, 
  emerald,
  steelblue,
  orange,
  red,
  cyan,
  blue,
  blue_mud,
  white_mud,
  aquamarine,
  saffron,
  mandarin,
  moss,
  git_red,
  git_green
}

