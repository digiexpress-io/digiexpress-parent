export interface DirentPermissions {
  name: string;
  privilege: string;
}

export interface FsDirentPermissionsProps {

}


export const permissions: DirentPermissions[] = [
  { name: 'John Smith (Me)', privilege: 'Read & write' },
  { name: 'Diana Hasselback', privilege: 'Read & write' },
  { name: 'office-staff', privilege: 'read' },
  { name: 'part-time staff', privilege: 'read' },
  { name: 'everyone', privilege: 'read' }
];
