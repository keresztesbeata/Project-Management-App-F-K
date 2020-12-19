package controller.team.single_team;

import model.InexistentDatabaseEntityException;
import model.UnauthorisedOperationException;
import model.team.TeamManager;
import model.team.exceptions.*;
import model.user.exceptions.InexistentUserException;
import model.user.exceptions.NoSignedInUserException;
import view.ErrorDialogFactory;
import view.team.single_team.TeamMembersPanel;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;

/**
 * This controller manages the TeamMembersPanel tab, displaying and updating the list of members of a team.
 */
public class TeamMembersController extends TeamController implements PropertyChangeListener {

    TeamMembersPanel membersPanel;

    /** Messages to confirm the removal of a member from the team. */
    private static final String CONFIRM_REMOVING_MEMBER_MESSAGE = "Are you sure you want to remove this member?";
    private static final String CONFIRM_REMOVING_MEMBER_TITLE = "Removing member";


    public TeamMembersController(TeamMembersPanel membersPanel,JFrame frame, int currentTeamId) {
        super(frame,currentTeamId);
        this.membersPanel = membersPanel;
        frame.addPropertyChangeListener(this);
        teamManager.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName()
                .equals(TeamManager.ChangablePropertyName.CHANGED_TEAM_MEMBERS.toString())) {
            membersPanel.updateMembersList();
        }else if(evt.getPropertyName().equals(TeamTabs.MEMBERS_TAB.toString())) {
            membersPanel.enableComponents(getManagerAccess());
        }
    }

    /**
     * Get the members of the team. The string array contains at least 1 element, which is the manager of the team.
     * @return the members of the current team
     */
    public String[] getTeamMembers() {
        try {
            return teamManager.getMembersOfTeam(currentTeamId);
        } catch (SQLException sqlException) {
            ErrorDialogFactory.createErrorDialog(sqlException,frame,"An internal error occurred, the members could not be displayed.");
        }
        return null;
    }

    public void addMember(String name) {
        try {
            teamManager.addMemberToTeam(currentTeamId,name);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } catch (InexistentTeamException | InexistentDatabaseEntityException databaseException) {
            ErrorDialogFactory.createErrorDialog(databaseException,frame,"An internal error occurred, the member " + name + " could not be added to the team.");
        } catch (UnauthorisedOperationException | NoSignedInUserException operationNotAllowedException) {
            ErrorDialogFactory.createErrorDialog(operationNotAllowedException,frame,"You are not allowed to add a new member to the team.");
        } catch (InexistentUserException inexistentUserException) {
            ErrorDialogFactory.createErrorDialog(inexistentUserException,frame,"The user " + name +" doesn't exist.\n Check that you have introduced the name correctly.");
          //  JOptionPane.showMessageDialog(frame,"The user " + name + " does not exist.","Rejected operation",JOptionPane.ERROR_MESSAGE);
        } catch (AlreadyMemberException alreadyMemberException) {
            ErrorDialogFactory.createErrorDialog(alreadyMemberException,frame,"The user " + name + " is already a member of this team.");
        }
    }
    public void removeMember(String name) {
            int choice = JOptionPane.showConfirmDialog(frame,CONFIRM_REMOVING_MEMBER_MESSAGE,CONFIRM_REMOVING_MEMBER_TITLE,JOptionPane.YES_NO_OPTION);
            if(choice == JOptionPane.YES_OPTION) {
                try {
                    teamManager.removeTeamMember(currentTeamId, name);
                } catch (InexistentTeamException | InexistentDatabaseEntityException | SQLException databaseException) {
                    ErrorDialogFactory.createErrorDialog(databaseException, frame, "An internal error occurred, the member " + name + " could not be removed.");
                } catch (UnauthorisedOperationException | NoSignedInUserException accessDeniedException) {
                    ErrorDialogFactory.createErrorDialog(accessDeniedException, frame, "You are not allowed to remove a member from this team.");
                } catch (UnregisteredMemberRemovalException unregisteredMemberRemovalException) {
                    ErrorDialogFactory.createErrorDialog(unregisteredMemberRemovalException, frame, "The user " + name + " is not a member of this team.");
                } catch (InexistentUserException inexistentUserException) {
                    //JOptionPane.showMessageDialog(frame, "The user " + name + " could not be removed, " +
                      //      "because it doesn't exist.","title",JOptionPane.ERROR_MESSAGE);
                    ErrorDialogFactory.createErrorDialog(inexistentUserException, frame, "The user \" + name + \" could not be removed, because it doesn't exist.");
                }catch (ManagerRemovalException managerRemovalException) {
                  // JOptionPane.showMessageDialog(frame,"The manager cannot be removed from the team.","Rejected operation",JOptionPane.ERROR_MESSAGE);
                    ErrorDialogFactory.createErrorDialog(managerRemovalException, frame, "The manager cannot be removed from the team.");
                }
            }
    }

}
