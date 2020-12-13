package model.team.repository.impl;

import model.UnauthorisedOperationException;
import model.User;
import model.team.InexistentTeamException;
import model.team.Team;
import model.team.TeamManager;
import model.team.repository.TeamRepository;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTeamRepository implements TeamRepository {
  private Connection c;

  // Save a new team.
  private static final String SAVE_TEAM_STATEMENT =
      "INSERT INTO Team (TeamName, ManagerId, " + "Code) VALUES (?, ?, ?)";
  private PreparedStatement saveTeamSt;

  // Delete a team.
  private static final String DELETE_TEAM_STATEMENT = "DELETE from Team WHERE TeamId = ?";
  private PreparedStatement deleteTeamSt;

  // Get a team with a given id.
  private static final String GET_TEAM_WITH_CODE_QUERY = "SELECT * from Team WHERE Code = ?";
  private PreparedStatement getTeamWithCodeSt;

  // Get a team with a given code.
  private static final String GET_TEAM_WITH_ID_QUERY = "SELECT * from Team WHERE TeamId = ?";
  private PreparedStatement getTeamWithIdSt;

  // Get teams of a given user.
  private static final String GET_TEAMS_OF_USER_QUERY =
      "SELECT t.TeamId, t.TeamName, t"
          + ".ManagerId, t.Code FROM Team t JOIN MemberToTeam mt ON mt.TeamId = t.TeamId WHERE mt"
          + ".MemberId = ?";
  private PreparedStatement getTeamsOfUserSt;

  // Set a new code for a team.
  private static final String SET_NEW_TEAMCODE_STATEMENT =
      "UPDATE team SET Code = ? WHERE " + "TeamId = ?";
  private PreparedStatement setNewCodeSt;

  // Add a new member to a team.
  private static final String ADD_TEAM_MEMBERSHIP_STATEMENT =
      "INSERT Into MemberToTeam (MemberId, " + "TeamId) VALUES (?, ?)";
  private PreparedStatement addTeamMembershipSt;

  // Remove a member from a team.
  private static final String REMOVE_TEAM_MEMBERSHIP_STATEMENT =
      "DELETE FROM MemberToTeam WHERE  " + "MemberId = ? AND TeamId = ?";
  private PreparedStatement removeTeamMembershipSt;

  // Remove all members of a team.
  private static final String REMOVE_ALL_TEAM_MEMBERS_STATEMENT =
      "DELETE FROM MemberToTeam WHERE TeamId = ?";
  private PreparedStatement removeAllTeamMembersSt;

  // Set new manager for team.
  private static final String SET_MANAGER_STATEMENT =
      "UPDATE team SET ManagerId = ? WHERE TeamId = ?";
  private PreparedStatement setManagerSt;

  public SqliteTeamRepository() {
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:project_management_app.db");
      prepareStatements();
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void prepareStatements() throws SQLException {
    saveTeamSt = c.prepareStatement(SAVE_TEAM_STATEMENT);
    deleteTeamSt = c.prepareStatement(DELETE_TEAM_STATEMENT);
    getTeamWithCodeSt = c.prepareStatement(GET_TEAM_WITH_CODE_QUERY);
    getTeamWithIdSt = c.prepareStatement(GET_TEAM_WITH_ID_QUERY);
    getTeamsOfUserSt = c.prepareStatement(GET_TEAMS_OF_USER_QUERY);
    setNewCodeSt = c.prepareStatement(SET_NEW_TEAMCODE_STATEMENT);
    addTeamMembershipSt = c.prepareStatement(ADD_TEAM_MEMBERSHIP_STATEMENT);
    removeTeamMembershipSt = c.prepareStatement(REMOVE_TEAM_MEMBERSHIP_STATEMENT);
    removeAllTeamMembersSt = c.prepareStatement(REMOVE_ALL_TEAM_MEMBERS_STATEMENT);
    setManagerSt = c.prepareStatement(SET_MANAGER_STATEMENT);
  }

  @Override
  public int saveTeam(Team team) throws SQLException {
    saveTeamSt.setString(1, team.getName());
    saveTeamSt.setInt(2, team.getManagerId());
    saveTeamSt.setString(3, team.getCode());
    saveTeamSt.execute();
    Optional<Team> savedTeam = getTeam(team.getCode());
    if (savedTeam.isPresent()) {
      team.setId(savedTeam.get().getId().get());
      return savedTeam.get().getId().get();
    } else {
      throw new SQLException("Saving team was unsuccesful");
    }
  }

  @Override
  public Optional<Team> getTeam(int teamId) throws SQLException {
    getTeamWithIdSt.setInt(1, teamId);
    ResultSet result = getTeamWithIdSt.executeQuery();
    if (result.next()) {
      int id = result.getInt("TeamId");
      String teamName = result.getString("TeamName");
      int managerId = result.getInt("ManagerId");
      String teamCode = result.getString("Code");
      return Optional.of(new Team(id, teamName, managerId, teamCode));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<Team> getTeam(String code) throws SQLException {
    getTeamWithCodeSt.setString(1, code);
    ResultSet result = getTeamWithCodeSt.executeQuery();
    if (result.next()) {
      int id = result.getInt("TeamId");
      String teamName = result.getString("TeamName");
      int managerId = result.getInt("ManagerId");
      String teamCode = result.getString("Code");
      return Optional.of(new Team(id, teamName, managerId, teamCode));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public List<Team> getTeamsOfUser(User user) throws SQLException {
    if (user.getId().isPresent()) {
      getTeamsOfUserSt.setInt(1, user.getId().get());
      ResultSet result = getTeamsOfUserSt.executeQuery();
      List<Team> usersTeams = new ArrayList<>();
      while (result.next()) {
        int id = result.getInt("TeamId");
        String teamName = result.getString("TeamName");
        int managerId = result.getInt("ManagerId");
        String teamCode = result.getString("Code");
        usersTeams.add(new Team(id, teamName, managerId, teamCode));
      }
      return usersTeams;
    } else {
      throw new IllegalArgumentException("The user whose teams are queried should have an " + "id");
    }
  }

  @Override
  public void deleteTeam(int teamId) throws SQLException {
    deleteAllMembersOfTeam(teamId);
    deleteTeamSt.setInt(1, teamId);
    deleteTeamSt.execute();
  }

  private void deleteAllMembersOfTeam(int teamId) throws SQLException {
    removeAllTeamMembersSt.setInt(1, teamId);
    removeAllTeamMembersSt.execute();
  }

  @Override
  public void joinTeam(int userId, int teamId) throws SQLException {
    addTeamMembershipSt.setInt(1, userId);
    addTeamMembershipSt.setInt(2, teamId);
    addTeamMembershipSt.execute();
  }

  @Override
  public void leaveTeam(int userId, int teamId) throws SQLException {
    removeTeamMembershipSt.setInt(1, userId);
    removeTeamMembershipSt.setInt(2, teamId);
    removeTeamMembershipSt.execute();
  }

  @Override
  public void setNewCode(int teamId, String newCode) throws SQLException {
    setNewCodeSt.setString(1, newCode);
    setNewCodeSt.setInt(2, teamId);
    setNewCodeSt.execute();
  }

  @Override
  public void setNewManagerPosition(int teamId, int managerId) throws SQLException {
    setManagerSt.setInt(1, managerId);
    setManagerSt.setInt(2, teamId);
    setManagerSt.execute();
  }

  public static void main(String[] args)
      throws SQLException, InexistentTeamException, UnauthorisedOperationException {
    //        TeamRepository repository = new SqliteTeamRepository();
    //        Team team = repository.getTeam("895621");
    //        System.out.println("Team with name " + team.getName() + " and id " + team.getId());
    //        repository.joinTeam(new User("Anna", "pass"), "895621");
    //        repository.leaveTeam(new User("Anna", "pass"), new Team(1, "aa", 5,
    //                "895621"));

    TeamManager manager = TeamManager.getInstance();
    //        // manager.createNewTeam("DBteam");
    //        List<Team> teamsOfUser1 = manager.getTeamsOfCurrentUser();
    //        System.out.println("Teams of user 1:");
    //        for (Team team : teamsOfUser1) {
    //            System.out.println(team.getName() + " " + team.getId().get());
    //        }
    // manager.regenerateTeamCode(2);
    //    manager.joinTeam("019404");
    //    manager.leaveTeam(3);
    //  manager.passManagerPosition(2, "maria22");
    manager.deleteTeam(3);
  }
}
