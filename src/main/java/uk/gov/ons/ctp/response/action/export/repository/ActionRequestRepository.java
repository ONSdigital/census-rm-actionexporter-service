package uk.gov.ons.ctp.response.action.export.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestInstruction;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * JPA repository for ActionRequest entities
 *
 */
@Repository
public interface ActionRequestRepository extends BaseRepository<ActionRequestInstruction, UUID> {

  /**
   *  Retrieve a list of collection exercise references to process.
   *
   * @return List of distinct exercise references.
   */
  @Query("SELECT DISTINCT(r.exerciseRef) FROM ActionRequestInstruction r WHERE r.dateSent IS NULL")
  List<String> findAllExerciseRefs();

  /**
   * Retrieve all action export requests not done for an actionType.
   *
   * @param actionType for which to return action requests.
   * @param exerciseRef for which to return action requests.
   * @return List ActionRequests not sent to external services previously for
   *         actionType.
   */
  List<ActionRequestInstruction> findByDateSentIsNullAndActionTypeAndExerciseRef(String actionType, String exerciseRef);

  /**
   * Retrieve a list of actionTypes
   *
   * @return List of distinct actionTypes
   */
  @Query("SELECT DISTINCT(r.actionType) FROM ActionRequestInstruction r")
  List<String> findAllActionType();

  /**
   * Retrieve an ActionRequestInstruction by actionId
   * @param actionId ActionRequestInstruction actionId to be retrieved
   * @return ActionRequestInstruction object
   */
  ActionRequestInstruction findByActionId(@Param("actionId") UUID actionId);

  /**
   * Update action request date sent for List of actionIds.
   *
   * @param actionIds List of ActionRequest actionIds to update
   * @param dateSent to set on each ActionRequest
   * @return int of affected rows
   */
  @Modifying
  @Transactional
  @Query("UPDATE ActionRequestInstruction r SET r.dateSent = :dateSent WHERE r.actionId IN :actionIds")
  int updateDateSentByActionId(@Param("actionIds") Set<UUID> actionIds, @Param("dateSent") Timestamp dateSent);

  /**
   * Retrieve actionIds where response required is true for List of actionIds.
   *
   * @param actionIds List of ActionRequest actionIds to check for response
   *          required.
   * @return actionIds of those where response required.
   */
  @Query("SELECT r.actionId FROM ActionRequestInstruction r WHERE r.responseRequired = "
      + "TRUE AND r.actionId IN :actionIds")
  List<UUID> retrieveResponseRequiredByActionId(@Param("actionIds") Set<UUID> actionIds);

  /**
   * Check repository for actionId existence
   *
   * @param actionId to check for existence
   * @return boolean whether exists
   */
  @Query(value = "select exists(select 1 from actionexporter.actionrequest where "
      + "actionid=:p_actionid)", nativeQuery = true)
  boolean tupleExists(@Param("p_actionid") UUID actionId);

}