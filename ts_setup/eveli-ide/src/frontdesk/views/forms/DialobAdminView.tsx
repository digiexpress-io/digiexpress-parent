import { DialobAdmin, DialobAdminConfig } from "@dialob/dashboard-material";
import { useMemo } from "react";
import { useConfig } from "../../context/ConfigContext";
import { useIntl } from "react-intl";
import { useSnackbar } from "notistack";
import { Container } from "@mui/system";


export const DialobAdminView: React.FC = () => {
  const config = useConfig();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const dialobAdminConfig: DialobAdminConfig | undefined = useMemo(() => {
    if(config.dialobComposerUrl){
      return {
        csrf : undefined,
        dialobApiUrl: config.dialobComposerUrl.split("/composer")[0],
        setLoginRequired: ()=>{},
        setTechnicalError: ()=>{},
        language: intl.locale 
      }    
    }else{
      return undefined;
    }
  }, [config.dialobComposerUrl, intl.locale]) 

  return (
    <Container maxWidth='xl' sx={{backgroundColor: 'white', pb: '20px'}}>
    {dialobAdminConfig && (
      <DialobAdmin showNotification={enqueueSnackbar} config={dialobAdminConfig}/> 
    )}
    </Container>
  )
}