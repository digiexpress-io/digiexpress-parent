import { Box, Typography, Paper } from "@mui/material";
import { FormattedMessage } from "react-intl";

import { UserActivity } from "taskclient/client-types";
import { useOrg } from "taskclient/hooks";

const ActivityItem: React.FC<{activity: UserActivity}> = ({ activity }) => {
  
  const { eventDate, eventType, subjectTitle } = activity;
  
  const activityText = <Typography>
    <FormattedMessage id={`core.myWork.recentActivities.events.${eventType}`} />{`: ${subjectTitle} on ${eventDate}`}
  </Typography>

  return (
    <>
      {activityText}
    </>
  )
}

const MyRecentActivity: React.FC = () => {
  const org = useOrg();
  const myActivities = org.state.iam.activity;
  
  return (
    <Box mt={2} display="flex" flexDirection="column">
      { myActivities.map( (activity: UserActivity) => (
        <Paper sx={{my: 2, p: 2}} elevation={4} key={activity.id}>
          <ActivityItem activity={activity} /> 
        </Paper>
      ))}
    </Box>
  )
}

export default MyRecentActivity;