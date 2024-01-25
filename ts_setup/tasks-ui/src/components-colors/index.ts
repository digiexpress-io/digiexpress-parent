// A bit too many colors... TODO:: lessen them

// https://coolors.co/ff595e-26c485-ffca3a-1982c4-6a4c93
const bittersweet: string = '#FF595E'; //red
const sunglow: string = '#FFCA3A';     //yellow
const ultraviolet: string = '#6A4C93'; //lillac

const emerald: string = '#26C485';         //green
const green_teal: string = '#10b981';      //rgb(16, 185, 129) green
const turquoise_topaz: string = '#14B8A6'; //rgb(20, 184, 166) green
const ocean_liner: string = '#109384';     //rgb(16, 147, 132) green

const steelblue: string = '#1982C4';   //blue
const orange: string = '#fb8500';      //orange96, 113, 150
const red: string = '#d00000';
const cyan: string = '#5048e5'; //rgb(80, 72, 229)';   //blue
const cyan_mud = 'rgba(80, 72, 229, 0.04)' //blue

const blue_mud = 'rgba(96, 113, 150, 0.5)' //blue


const blue = '#0088FE' //blue
const blue_mud2 = '#E9E9EA'; // blue
const white_mud = '#F5F5F5'; //white
const blueberry_whip = '#d1d5db' //rgb(209, 213, 219) white 

const bullfighters_red = '#D14343'; //rgb(209, 67, 67) red
const purple_zergling = '#a0548b'; //rgb(160, 84, 139) red

const cocktail_green = '#91bc24' //rgb(145, 188, 36) green

const aquamarine = '#00C49F'; //green
const saffron = '#FFBB28'; //yellow
const mandarin = '#FF8042' //red
const moss = '#A1A314'; //green

const git_red = 'rgb(254, 237, 240)';
const git_green = 'rgb(230, 255, 237)';

const purple = 'rgba(80, 72, 229, 0.9)';
const grey = '#39393D';
const sambucus = '#121828' //rgb(18, 24, 40) - rgb(17, 24, 39)
const wash_me = '#f9fafc' //rgb(249, 250, 252) // white


const grey_light = 'RGB(209, 213, 219)';
const turquoise = '#14B8A6'; //green


const PALETTE = { red: bittersweet, green: emerald, yellow: sunglow, blue: steelblue, violet: ultraviolet };
const PALETTE_COLORS = Object.values(PALETTE);

function withColors<T>(input: T[], colorIndex?: number): { color: string, value: T }[] {
  const result: { color: string, value: T }[] = [];
  
  let index = colorIndex ?? 0;
  if(index > PALETTE_COLORS.length - 1) {
    index = 0;
  }

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
  cocktail_green,
  purple_zergling,
  ocean_liner,
  turquoise_topaz,
  bullfighters_red,
  green_teal,
  wash_me,
  sambucus,
  turquoise,
  grey_light,
  cyan_mud,
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
  git_green,
  blueberry_whip
}

