
export const classes = {
  formControl: {
    margin: '1em',
    minWidth: 120,
    maxWidth: 300,
  },
  chips: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  chip: {
    margin: 2,
  },
  noLabel: {
    marginTop: '3em',
  },
  accordionSummary: {
    display: "flex",
    "& .Mui-expanded": {
      marginBottom: - 1,
      marginTop: 0
    }
  },
  accordionTitle: {
    fontWeight: 'bolder',
    width: "max-content",
    mr: 2
  },
  accordionDetails: {
    pt: 0
  },
  taskRoleList: {
    display: "flex",
    flexWrap: "wrap",
    gap: 1,
    paddingTop: 0,
    paddingBottom: 1,
    paddingX: 1
  },
  taskRoleFieldset: {
    borderWidth: 1,
    borderStyle: 'solid',
    borderRadius: 10,
    width: "90%",
    marginBottom: 8,
    minHeight: 64
  },
  taskRoleLegend: {
    marginLeft: 8,
    paddingLeft: 24
  },
  keywordChip: {
    width: "max-content",
    ml: 1
  },
};

export function useMuiClasses() {
  return { classes };
}