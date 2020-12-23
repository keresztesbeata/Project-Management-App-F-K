package controller.project.single_project;

import model.InexistentDatabaseEntityException;
import model.UnauthorisedOperationException;
import model.project.Project;
import model.project.ProjectManager;
import model.project.exceptions.DuplicateProjectNameException;
import model.project.exceptions.IllegalProjectStatusChangeException;
import model.project.exceptions.InexistentProjectException;
import model.team.TeamManager;
import model.team.exceptions.UnregisteredMemberRoleException;
import model.user.User;
import model.user.UserManager;
import model.user.exceptions.InexistentUserException;
import model.user.exceptions.NoSignedInUserException;
import view.ErrorDialogFactory;
import view.project.single_project.ProjectDetailsPanel;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Manages the ProjectDetails panel, being responsible for listing and updating the project's
 * details.
 *
 * @author Beata Keresztes
 */
public class ProjectDetailsController implements PropertyChangeListener {

  private TeamManager teamManager;
  private UserManager userManager;
  private ProjectManager projectManager;
  private Project project;
  private ProjectDetailsPanel panel;

  public ProjectDetailsController(Project project, ProjectDetailsPanel panel) {
    teamManager = TeamManager.getInstance();
    userManager = UserManager.getInstance();
    projectManager = ProjectManager.getInstance();
    projectManager.addPropertyChangeListener(this);
    this.project = project;
    this.panel = panel;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName()
        .equals(ProjectManager.ProjectChangeablePropertyName.UPDATE_PROJECT.toString())) {
      setProject();
      panel.updatePanel();
    } else if (evt.getPropertyName()
        .equals(ProjectManager.ProjectChangeablePropertyName.SET_PROJECT_STATUS.toString())) {
      setProject();
      selectProjectStatusButtons();
      panel.updateStatusLabel();
    }
  }

  private void setProject() {
    try {
      project = projectManager.getProjectById(project.getId());
    } catch (InexistentProjectException | InexistentDatabaseEntityException | SQLException e) {
      ErrorDialogFactory.createErrorDialog(e, null, "The project could not be updated.");
    }
  }

  public boolean isSupervisor() {
    try {
      return userManager.getCurrentUser().get().getId() == project.getSupervisorId();
    } catch (InexistentDatabaseEntityException e) {
      ErrorDialogFactory.createErrorDialog(e, null, null);
    }
    return false;
  }

  private boolean isAssignee() {
    try {
      return userManager.getCurrentUser().get().getId() == project.getAssigneeId();
    } catch (InexistentDatabaseEntityException e) {
      ErrorDialogFactory.createErrorDialog(e, null, null);
    }
    return false;
  }

  public boolean enableEditing() {
    return (isSupervisor() && (project.getStatus() != Project.ProjectStatus.FINISHED));
  }

  public User getProjectAssignee() {
    try {
      return userManager.getUserById(project.getAssigneeId());
    } catch (SQLException sqlException) {
      ErrorDialogFactory.createErrorDialog(sqlException, null, null);
    }
    return null;
  }

  public User getProjectSupervisor() {
    try {
      return userManager.getUserById(project.getSupervisorId());
    } catch (SQLException sqlException) {
      ErrorDialogFactory.createErrorDialog(sqlException, null, null);
    }
    return null;
  }

  public String getProjectTitle() {
    return project.getTitle();
  }

  public String getProjectDescription() {
    if (project.getDescription().isPresent()) {
      return project.getDescription().get();
    }
    return null;
  }

  public LocalDate getProjectDeadline() {
    return project.getDeadline();
  }

  public String getStatus() {
    return project.getStatus().toString();
  }

  public List<User> getTeamMembers() {
    try {
      return teamManager.getMembersOfTeam(project.getTeamId());
    } catch (SQLException sqlException) {
      ErrorDialogFactory.createErrorDialog(sqlException, null, null);
      return Collections.emptyList();
    }
  }

  public void saveProject(
      String title, String assignee, String supervisor, LocalDate deadline, String description) {
    try {
      projectManager.updateProject(
          project.getId(), title, assignee, supervisor, deadline, description);
      displaySuccessfulSaveMessage();
    } catch (InexistentDatabaseEntityException | SQLException | InexistentProjectException e) {
      ErrorDialogFactory.createErrorDialog(
          e,
          null,
          "The project \"" + project.getTitle() + "\" could not be found in the database.");
    } catch (NoSignedInUserException
        | UnauthorisedOperationException
        | InexistentUserException
        | UnregisteredMemberRoleException e) {
      ErrorDialogFactory.createErrorDialog(
          e, null, "You don't have access to edit the project \"" + project.getTitle() + "\"");
    } catch (DuplicateProjectNameException e) {
      ErrorDialogFactory.createErrorDialog(
          e, null, "The project with title\"" + project.getTitle() + "\" already exists");
    }
  }

  private void displaySuccessfulSaveMessage() {
    JOptionPane.showMessageDialog(
        null,
        "The project was updated successfully!",
        "Changes saved",
        JOptionPane.INFORMATION_MESSAGE);
  }

  private void displayIllegalStateErrorDialog(
      IllegalProjectStatusChangeException e, Project.ProjectStatus newState) {
    ErrorDialogFactory.createErrorDialog(
        e,
        null,
        "You cannot set the project from status " + project.getStatus() + " to " + newState);
  }

  /**
   * Changes the status of the project respecting the valid transition between the states.
   * @param newStatus the new status set by the user
   * @return boolean = true if the project could be set to the new state, otherwise false
   */
  public boolean setProjectStatus(String newStatus) {
    try {
      if (newStatus.equals(Project.ProjectStatus.FINISHED.toString())) {
        projectManager.acceptAsFinished(project.getId());
      } else if (project.getStatus() == Project.ProjectStatus.TURNED_IN) {
        // the new state can only be "to do" or "in progress"
        if (isSupervisor()) {
          projectManager.discardTurnIn(project.getId(), Project.ProjectStatus.valueOf(newStatus));
        } else if (isAssignee()) {
          projectManager.undoTurnIn(project.getId(), Project.ProjectStatus.valueOf(newStatus));
        }
      } else if (newStatus.equals(Project.ProjectStatus.IN_PROGRESS.toString())) {
        projectManager.setProjectInProgress(project.getId());
      } else if (newStatus.equals(Project.ProjectStatus.TO_DO.toString())) {
        projectManager.setProjectAsToDo(project.getId());
      }
      return true;
    } catch (NoSignedInUserException
        | InexistentDatabaseEntityException
        | SQLException
        | InexistentProjectException e) {
      ErrorDialogFactory.createErrorDialog(e, null, null);
    } catch (IllegalProjectStatusChangeException e) {
      displayIllegalStateErrorDialog(e, Project.ProjectStatus.valueOf(newStatus));
    } catch (UnauthorisedOperationException e) {
      String message = null;
      if (newStatus.equals(Project.ProjectStatus.FINISHED.toString())) {
        message = "Only the supervisor can set the project as finished.";
      } else if (newStatus.equals(Project.ProjectStatus.IN_PROGRESS.toString())
          || newStatus.equals(Project.ProjectStatus.TURNED_IN.toString())) {
        message = "Only the assignee can set the project as in progress or turn in the project.";
      }
      ErrorDialogFactory.createErrorDialog(e, null, message);
    }
    return false;
  }

  /**
   * Depending on the status of the project, only those options are shown which represent a valid change of state.
   * * Finished projects cannot be revoked or edited.
   * * Turned in projects can be undone by either the assignee or supervisor, but only the supervisor can set it as finished.
   * * In progress projects can be changed only by the assignee, setting it back to "to do" or turning it in.
   * * To do projects can be changed also only by the assignee, to "in progress" or turned in.
   */
  public void selectProjectStatusButtons() {
    boolean enableTodo = false;
    boolean enableInProgress = false;
    boolean enableTurnedIn = false;
    boolean enableFinished = false;
    if(isSupervisor()) {
      if (project.getStatus() == Project.ProjectStatus.TURNED_IN) {
        enableFinished = enableInProgress = enableTodo = true;
      }
    } else if (isAssignee()) {
      if (project.getStatus() == Project.ProjectStatus.TURNED_IN) {
        enableInProgress = enableTodo = true;
      } else if (project.getStatus() == Project.ProjectStatus.IN_PROGRESS) {
        enableTodo = enableTurnedIn = true;
      } else if (project.getStatus() == Project.ProjectStatus.TO_DO) {
        enableInProgress = enableTurnedIn = true;
      }
    }
    panel.enableProjectStatusButtons(enableTodo,enableInProgress,enableTurnedIn,enableFinished);
  }

  public void deleteProject() {
    // todo
  }
}