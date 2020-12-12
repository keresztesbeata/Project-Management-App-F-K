package main.java.model.team.repository;

import main.java.model.User;
import main.java.model.team.Team;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public interface TeamRepository {
  /** Saves the new team in the database and returns the id of the team in the database. */
  int saveTeam(Team team) throws SQLException;

  @Nullable
  Team getTeam(String code) throws SQLException;

  List<Team> getTeamsOfUser(User user) throws SQLException;

  void deleteTeam(Team team);

  /**
   * Adds user to the members of the team with id teamID
   *
   * @param teamId is the id of the team which user wants to join. Requirement: The team must exist
   *     in the database.
   * @param userId is the new member's id.
   */
  void joinTeam(int userId, int teamId) throws SQLException;

  /**
   * Removes user from the members of the team with id teamID
   *
   * @param teamId is the id of the team which user wants to leave. Requirement: The team must exist
   *     in the database.
   * @param userId is the new member's id.
   */
  void leaveTeam(int userId, int teamId) throws SQLException;

  /**
   * Sets the new code for the specified team.
   *
   * @param teamId is the id of the team to update. requirement: The team must exist in the
   *     database.
   * @param newCode is the new code to set.
   */
  void setNewCode(int teamId, String newCode) throws SQLException;
}
