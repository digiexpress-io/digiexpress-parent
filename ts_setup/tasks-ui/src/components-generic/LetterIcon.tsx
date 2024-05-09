import { Avatar } from "@mui/material";
import { sambucus } from "components-colors";


export const LetterIcon: React.FC<{ children: React.ReactNode, transparent?: boolean}> = ({children, transparent}) => {
  return (<Avatar sx={{
    width: '24px', height: '24px',
    fontSize: '0.9rem',
    color: transparent ? undefined : sambucus,
    fontWeight: 'bold'
  }}>
    <span>{children}</span>
  </Avatar>);
}
