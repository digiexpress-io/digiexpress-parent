import React, { ReactNode, useEffect, useState } from 'react';
import { Formik, Form, Field } from 'formik';
import {
  TextField, Grid2, MenuItem, Chip, InputLabel, Typography, ListItemText, Checkbox,
  Stack, Box, Paper, Accordion, AccordionSummary, AccordionDetails, Badge, Autocomplete,
  useTheme,
  alpha,
} from '@mui/material';

import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import AttachmentIcon from '@mui/icons-material/Attachment';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';

import { injectIntl, defineMessages, WrappedComponentProps, FormattedMessage, FormattedDate } from 'react-intl';
import { toZonedTime } from 'date-fns-tz';
import { useNavigate } from "react-router-dom";


import { PageLeavingConfirmation } from '../../components/PageLeaveConfirmation';
import { StatusComponent } from '../../components/task/Status';
import { Priority } from '../../components/task/Priority';
import { Datepicker } from '../../components/Datepicker';
import { TaskLinkProps } from '../../components/task/TaskLinkComponent';

import { withRouter, WithRouterProps } from '../../hooks/withRouter';

import { TaskRoleDialog } from './TaskRoleDialog';
import { AttachmentTable } from './AttachmentTable';
import { CommentThread } from './CommentThread';

import { Attachment, User } from '../../types';
import { UserGroup } from '../../types/UserGroup';
import { Task, TaskStatus } from '../../types/task/Task';
import { GroupMember } from '../../types/GroupMember';
import { Comment } from '../../types/task/Comment';

import { useAttachmentConfig } from '../../context/AttachmentContext';
import { ComponentResolver } from '../../context/ComponentResolver';
import * as Yup from 'yup';

import * as Burger from '@/burger';
import { TaskFeedback } from './TaskFeedback';
import { StatusIndicator } from '../../../feedback/status-indicator';


const AttachmentTableWrapper: React.FC<{ editTask: Task, readonly: boolean }> = ({ editTask, readonly }) => {
  const taskId = editTask.id;
  const attachmentContext = useAttachmentConfig();
  const [attachments, setAttachments] = useState<Attachment[]>([]);

  useEffect(() => {
    if (taskId) {
      attachmentContext.loadAttachments(taskId)
        .then((result: Attachment[]) => {
          setAttachments(result);
        });
    }
    else {
      setAttachments([]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [taskId]);

  return (
    <Accordion>
      <AccordionSummary
        expandIcon={<ExpandMoreIcon />}
        aria-controls="panel1bh-content"
        id="panel1bh-header"
        sx={classes.accordionSummary}
      >
        <Typography sx={classes.accordionTitle}>
          <FormattedMessage id="attachmentView.title" />
        </Typography>
        <Badge badgeContent={attachments?.length} color='secondary'>
          <AttachmentIcon />
        </Badge>
      </AccordionSummary>
      <AccordionDetails sx={classes.accordionDetails}>
        {!!editTask.id &&
          <AttachmentTable taskId={editTask.id} readonly={readonly} attachments={attachments} setAttachments={setAttachments} />
        }
      </AccordionDetails>
    </Accordion>
  )
}

const NewTaskAccordianMsg: React.FC<{ id: string }> = ({ id }) => {
  const theme = useTheme();
  return (
    <Paper sx={{ p: 2, display: 'flex', alignItems: 'center', backgroundColor: alpha(theme.palette.uiElements.main, 0.05) }}>
      <InfoOutlinedIcon sx={{ mr: 1, color: theme.palette.uiElements.main }} />
      <Typography variant='subtitle2'><FormattedMessage id={id} /></Typography>
    </Paper>)
}


const messages = defineMessages(
  {
    requiredError: {
      id: "error.valueRequired"
    },
    minLengthError: {
      id: "error.minTextLength"
    },
    statusOpenError: {
      id: "error.statusOpenError"
    },
    cancel: {
      id: 'taskButton.cancel'
    }

  }
)


const classes = {
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

type Props = {
  id: string
  groups: UserGroup[]
  getUsers: (groupName: string[]) => Promise<GroupMember[]>
  editTask: Task
  handleSubmit: (task: Task) => void
  cancel: () => void
  componentResolver?: ComponentResolver
  externalThreads?: boolean
  comments: Comment[]
  reloadComments: () => void
  userSelectionFree?: boolean
  currentUser: Partial<User>
  supressConfirmation?: boolean | undefined
}

type AllProps = Props & WrappedComponentProps & WithRouterProps;
type State = {
  userList: GroupMember[];
  dialogOpen: boolean;
}

const minLength = 3;


const FeedbackButton: React.FC<{ taskId: number | undefined }> = ({ taskId }) => {
  const navigate = useNavigate();

  function handleFeedback() {
    navigate(`/feedback/${taskId}`);
  }

  return (<Burger.SecondaryButton label='task.form.feedback.manage' onClick={handleFeedback} />);
}

class TaskCreateInternal extends React.Component<AllProps, State> {
  formRef = React.createRef<any>();

  validationSchema = Yup.object().shape({
    subject: Yup.string()
      .required(this.props.intl.formatMessage(messages.requiredError))
      .min(3, this.props.intl.formatMessage(messages.minLengthError, { minLength })),
    assignedUser: Yup.string()
      .test('assignedUser-validation', this.props.intl.formatMessage(messages.statusOpenError), function (value) {
        const { status } = this.parent;
        if (!value && status === 'OPEN') {
          return false;
        }
        return true;
      })
  });

  constructor(props: AllProps) {
    super(props);
    this.state = {
      userList: [],
      dialogOpen: false
    }
  }

  componentDidMount(): void {
    let task = this.props.editTask;
    this.getGroupUsers(task.assignedRoles);
  }

  formatTimestamp = (time: any) => {
    if (time) {
      const now = new Date();
      const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
      const zonedDate = toZonedTime(time, timeZone);
      const showYear = zonedDate.getFullYear() !== now.getFullYear();
      return (
        <FormattedDate
          value={zonedDate.toUTCString()}
          year={(showYear && 'numeric') || undefined}
          month='long'
          day='numeric'
          hour='2-digit'
          minute='2-digit'
        />
      )
    }
    return "-";
  }

  createGroupMenuItems = (roles?: string[] | null) => {
    let result: JSX.Element[] = [];
    this.props.groups.forEach((group: any) => {
      result.push(
        <MenuItem key={group.id} value={group.id}>
          <Checkbox checked={!!roles && roles.indexOf(group.id) > -1} disableRipple={true} />
          <ListItemText primary={group.groupName || group.id} />
        </MenuItem>)
    });
    return result;
  }
  getGroupUsers = (selectedGroups?: string[] | null) => {
    if (selectedGroups && selectedGroups.length > 0) {
      this.props.getUsers(selectedGroups)
        .then((users: any) => this.setState({ userList: users }));
    }
    else {
      this.setState({ userList: [] });
    }
  }

  renderTaskLink = (props: TaskLinkProps) => {
    if (this.props.componentResolver?.taskLinkResolver)
      return this.props.componentResolver.taskLinkResolver(props);
    else
      return undefined;
  }

  taskFromValues = (values: any): Task => {
    const { editTask } = this.props;
    return {
      id: editTask?.id,
      priority: values.priority,
      subject: values.subject,
      description: values.description,
      dueDate: values.dueDate,
      status: values.status,
      assignedUser: values.assignedUser,
      assignedUserEmail: values.assignedUserEmail,
      version: editTask?.version,
      keyWords: editTask?.keyWords,
      clientIdentificator: values.clientIdentificator,
      assignedRoles: values.assignedRoles
    }
  }

  openDialog = () => {
    this.setState({ dialogOpen: true });
  }
  onDialogClose = () => {
    this.setState({ dialogOpen: false });
  }
  onDialogAccept = () => {
    this.setState({ dialogOpen: false });

  }

  handleRoleChange = (roles: UserGroup[],
    setFieldValue: (field: string, value: any, shouldValidate?: boolean | undefined) => void) => {
    const groupList = roles.map(r => r.id);
    setFieldValue("assignedRoles", groupList);
    this.getGroupUsers(groupList);
  }

  findRoleDescription = (role: string) => {
    return this.props.groups.find((group: any) => group.id === role)?.groupName || role;
  }

  renderRoles = (roles: string[]): ReactNode => {
    return (<Box sx={classes.chips}>
      {roles.map((value) => (
        <Chip key={value} label={this.findRoleDescription(value)} sx={classes.chip} />
      ))}
    </Box>);
  }

  renderTextRoles = (roles: string[]): string => {
    return roles.map(role => this.findRoleDescription(role)).join(", ");
  }

  getTaskKeywords = (editTask: Task) => {
    return editTask.keyWords!.flatMap(element => element.split(','));
  }

  handleStatusCallback = async (newValue: string) => {
    if (newValue === "OPEN" && (this.formRef?.current?.values.status === "NEW" || this.formRef?.current?.values.status === undefined)) {
      await this.formRef.current.setFieldValue("assignedUser", this.props?.currentUser?.name || "");
      await this.formRef.current.setFieldValue("assignedUserEmail", this.props?.currentUser?.email || "");
    }
  }

  render() {
    const { editTask, handleSubmit, groups, externalThreads, comments, reloadComments } = this.props;
    const { formatMessage } = this.props.intl;
    const readonly = (editTask.status === TaskStatus.COMPLETED ||
      editTask.status === TaskStatus.REJECTED);

    return (
      <Formik
        initialValues={{
          priority: editTask.priority,
          subject: editTask.subject || '',
          description: editTask.description || '',
          dueDate: editTask.dueDate,
          status: editTask.status,
          assignedUser: editTask.assignedUser || '',
          assignedUserEmail: editTask.assignedUserEmail || '',
          clientIdentificator: editTask.clientIdentificator || '',
          assignedRoles: editTask.assignedRoles || []
        }}
        validationSchema={this.validationSchema}
        enableReinitialize={true}
        onSubmit={(values) => {
          handleSubmit(this.taskFromValues(values));
        }}
        innerRef={this.formRef}
      >
        {
          ({ values, submitForm, isSubmitting, errors, isValid, dirty, setFieldValue }) => (
            <Form>
              <PageLeavingConfirmation navigate={(path) => this.props.navigate(path)}
                navigationConfirmationRequired={() => dirty && this.props.supressConfirmation !== true}
              />
              <Paper elevation={2} sx={{ p: 2, mb: 2 }}>
                <Grid2 container spacing={2} alignItems="center">
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    {editTask.keyWords && editTask.keyWords.length > 0 && (
                      <Box display='flex' alignItems='center'>
                        <InputLabel>{formatMessage({ id: 'taskDialog.category' })}: </InputLabel>
                        <Chip
                          label={this.getTaskKeywords(editTask).includes('Protected') ? formatMessage({ id: 'Protected' }) : formatMessage({ id: 'Normal' })}
                          color={this.getTaskKeywords(editTask).includes('Protected') ? 'error' : 'primary'}
                          sx={classes.keywordChip}
                        />
                      </Box>
                    )}
                    {(!editTask.keyWords || editTask.keyWords.length === 0) && (
                      <InputLabel>{formatMessage({ id: 'taskDialog.category' })}: -</InputLabel>
                    )}
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <Typography>
                      <FormattedMessage id={'task.created'} />:&nbsp;{this.formatTimestamp(editTask.created)}
                    </Typography>
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <Field
                      name='dueDate'
                      component={Datepicker}
                      disableMaskedInput
                      label={formatMessage({ id: 'taskDialog.dueDate' })}
                      fullWidth={true}
                      readonly={readonly}
                    />
                  </Grid2>
                </Grid2>
                <Grid2 container spacing={2} alignItems="top" sx={{ mt: 1 }}>
                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <Field
                      name='clientIdentificator' as={TextField}
                      label={formatMessage({ id: 'taskDialog.clientIdentificator' })}
                      fullWidth={true}
                      inputProps={{
                        readOnly: readonly
                      }}
                    />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <Field
                      name='subject' as={TextField}
                      label={formatMessage({ id: 'taskDialog.subject' })}
                      required
                      error={!!errors.subject}
                      helperText={errors.subject}
                      fullWidth={true}
                      inputProps={{
                        readOnly: readonly
                      }}
                    />
                  </Grid2>
                </Grid2>
                <Grid2 container spacing={2} alignItems="center" sx={{ mt: 1 }}>
                  {!!editTask.taskLinks && editTask.taskLinks.length > 0 &&
                    <Grid2 size={{ xs: 12, md: 6 }}>
                      <Box display="flex" gap={1} flexWrap="wrap">
                        {editTask.taskLinks.map(taskLink => {
                          return this.renderTaskLink({ link: taskLink, taskId: editTask.id })
                        })}
                      </Box>
                    </Grid2>
                  }
                  <Grid2 size={{ xs: 12, md: !!editTask.taskLinks && editTask.taskLinks.length > 0 ? 6 : 12 }}>
                    {editTask.keyWords && editTask.keyWords.length > 0 && (
                      <Box display='flex' alignItems='center'>
                        <InputLabel>{formatMessage({ id: 'taskDialog.source' })}: </InputLabel>

                        <Chip
                          label={this.getTaskKeywords(editTask).includes('Manual') ? formatMessage({ id: 'Internal' }) : formatMessage({ id: 'CustomerCreated' })}
                          color='primary'
                          sx={classes.keywordChip}
                        />
                      </Box>
                    )}

                  </Grid2>
                </Grid2>
              </Paper>


              <Grid2 container spacing={2}>
                <Grid2 size={{ xs: 12 }}>
                  {editTask.id && externalThreads ?
                    <Accordion>
                      <AccordionSummary
                        expandIcon={<ExpandMoreIcon />}
                        aria-controls="panel1bh-content"
                        id="panel1bh-header"
                        sx={classes.accordionSummary}
                      >
                        <Typography sx={classes.accordionTitle}>
                          <FormattedMessage id="externalComments" />
                        </Typography>
                        <Badge badgeContent={comments?.filter(comment => comment.external === true).length} color='warning'>
                          <ChatBubbleOutlineIcon />
                        </Badge>
                      </AccordionSummary>
                      <AccordionDetails sx={classes.accordionDetails}>
                        <CommentThread
                          task={editTask}
                          isExternalThread={true}
                          comments={comments}
                          loadData={reloadComments}
                          isThreaded={false}
                        />

                      </AccordionDetails>
                    </Accordion>
                    : <NewTaskAccordianMsg id='task.comments.external.createTask' />
                  }
                </Grid2>
                <Grid2 size={{ xs: 12 }}>
                  {editTask.id && externalThreads ?
                    <Accordion>
                      <AccordionSummary
                        expandIcon={<ExpandMoreIcon />}
                        aria-controls="panel1bh-content"
                        id="panel1bh-header"
                      //sx={classes.accordionSummary}
                      >
                        <Typography sx={classes.accordionTitle}>
                          <FormattedMessage id="task.feedback.published" />
                        </Typography>
                        {/* <Badge badgeContent={comments?.filter(comment => comment.external === true).length} color='success'><SupportAgentIcon /></Badge> */}
                        <Badge badgeContent={<StatusIndicator size='SMALL' taskId={editTask.id + ""} />}><SupportAgentIcon /></Badge>
                      </AccordionSummary>
                      <AccordionDetails sx={classes.accordionDetails}>
                        <TaskFeedback taskId={editTask.id! + ''} />
                      </AccordionDetails>
                    </Accordion>
                    : <NewTaskAccordianMsg id='task.comments.external.createTask' />
                  }
                </Grid2>
                <Grid2 size={{ xs: 12 }}>
                  {editTask.id ? <AttachmentTableWrapper readonly={readonly} editTask={editTask} /> : <NewTaskAccordianMsg id='task.attachments.createTask' />}
                </Grid2>
                <Grid2 size={{ xs: 12 }}>
                  {editTask.id ?
                    <Accordion>
                      <AccordionSummary
                        expandIcon={<ExpandMoreIcon />}
                        aria-controls="panel1bh-content"
                        id="panel1bh-header"
                        sx={classes.accordionSummary}
                      >
                        <Typography sx={classes.accordionTitle}>
                          <FormattedMessage id="internalComments" />
                        </Typography>
                        <Badge badgeContent={comments?.filter(comment => comment.external === false).length} color='primary'>
                          <ChatBubbleOutlineIcon />
                        </Badge>
                      </AccordionSummary>
                      <AccordionDetails sx={classes.accordionDetails}>

                        <CommentThread
                          task={editTask}
                          isExternalThread={typeof externalThreads === 'undefined' ? externalThreads : false}
                          comments={comments}
                          loadData={reloadComments}
                          isThreaded={true}
                        />

                      </AccordionDetails>
                    </Accordion>
                    : <NewTaskAccordianMsg id='task.comments.internal.createTask' />}
                </Grid2>
              </Grid2>


              <Paper elevation={2} sx={{ p: 2, mb: 2, mt: 2 }}>
                <Grid2 container spacing={2} alignItems="top">
                  {!!groups.length &&
                    <Grid2 size={{ xs: 12, md: 6 }}>
                      <Box display="flex" alignItems="center">
                        <fieldset style={classes.taskRoleFieldset}>
                          <legend style={classes.taskRoleLegend}>
                            <InputLabel size='small' shrink={true}><FormattedMessage id='taskDialog.assignedTo' /></InputLabel>
                          </legend>
                          <Box id='task-role-list' sx={classes.taskRoleList}>
                            {values.assignedRoles.map((value: any) => (
                              <Chip key={value} label={this.findRoleDescription(value)} />
                            ))}
                          </Box>
                        </fieldset>

                      </Box>
                      <Burger.PrimaryButton onClick={() => { this.openDialog() }} label='button.editRoles' />

                    </Grid2>
                  }
                  {<Grid2 size={{ xs: 12, md: !!groups.length ? 6 : 12 }} sx={{ mt: 1 }}>
                    {!readonly &&
                      <Autocomplete
                        id="assignedUser"
                        freeSolo
                        options={this.state.userList}
                        getOptionLabel={option => (typeof option === "string") ? option : option.userName}
                        value={{ userName: values.assignedUser, userEmail: values.assignedUserEmail }}
                        onInputChange={(event, newInputValue) => {
                          if (newInputValue !== values.assignedUser) {
                            setFieldValue("assignedUserEmail", this.state.userList.find(el => el.userName === newInputValue)?.userEmail || '');
                          }
                          setFieldValue("assignedUser", newInputValue);
                        }}
                        renderInput={(params) => (
                          <TextField {...params}
                            name='assignedUser'
                            fullWidth={true}
                            label={formatMessage({ id: 'taskDialog.assignedUser' })}
                            InputLabelProps={{
                              shrink: true,
                            }}
                            error={!!errors.assignedUser}
                            helperText={errors.assignedUser}
                          />
                        )}
                      />
                    }
                    {readonly &&
                      <TextField
                        name='assignedUser'
                        value={values.assignedUser}
                        fullWidth={true}
                        inputProps={{
                          readOnly: readonly
                        }}
                        label={formatMessage({ id: 'taskDialog.assignedUser' })}
                        InputLabelProps={{
                          shrink: true,
                        }}
                      >
                      </TextField>
                    }
                  </Grid2>
                  }

                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <Field
                      name='status' as={StatusComponent}
                      label={formatMessage({ id: 'taskDialog.status' })}
                      readonly={readonly}
                      handleCallback={this.handleStatusCallback}
                    />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <Field
                      name='priority' as={Priority}
                      label={formatMessage({ id: 'taskDialog.priority' })}
                      readonly={readonly}
                    />
                  </Grid2>
                </Grid2>
              </Paper>

              <Paper elevation={2} sx={{ p: 2, mb: 2 }}>
                <Grid2 container spacing={2}>
                  {editTask?.id &&
                    <Grid2 size={{ xs: 12, md: 6 }} container justifyContent="flex-start">
                      <Typography variant="caption" display="block" gutterBottom>
                        <FormattedMessage id={'task.updated'} />:&nbsp;{this.formatTimestamp(editTask.updated)}&nbsp;&nbsp;
                        {editTask.updaterId || ''}
                      </Typography>
                    </Grid2>
                  }
                </Grid2>
              </Paper>

              <Box sx={{ position: 'sticky', bottom: 10, width: 'fit-content', float: 'right' }}>
                <Paper elevation={2} sx={{ padding: 1, marginRight: 2 }}>
                  <Stack direction="row" spacing={1} justifyContent='flex-end'>
                    <Burger.SecondaryButton onClick={() => this.props.navigate('/ui/tasks')} label={'taskButton.cancel'} />
                    {(!editTask.keyWords || editTask.keyWords.length === 0) && (
                      <Box display='flex' gap={1}>
                        <Burger.SecondaryButton label='task.form.review' onClick={() => { }} />
                        <FeedbackButton taskId={editTask.id} />
                      </Box>
                    )}
                    {!readonly && <Burger.PrimaryButton disabled={isSubmitting || !isValid || !dirty} onClick={submitForm} label='taskButton.accept' />}
                  </Stack>
                </Paper>
              </Box>
              {this.state.dialogOpen && <TaskRoleDialog
                assignedRoles={values.assignedRoles} groups={this.props.groups}
                acceptDialog={(roles: UserGroup[]) => { this.handleRoleChange(roles, setFieldValue); this.onDialogClose(); }}
                closeDialog={this.onDialogClose} />
              }
            </Form>
          )
        }
      </Formik>

    );
  }
}

export const TaskCreate = injectIntl(withRouter(TaskCreateInternal))