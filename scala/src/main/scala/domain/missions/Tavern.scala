package domain.missions

import domain.Team

import scala.util.{Failure, Success, Try}

type MissionPickCriteria = (Team, Team) => Boolean

case class Tavern(missions: Set[Mission]) {
    def bestMissionFor(team: Team, criteria: MissionPickCriteria): Option[Mission] =
        missions
            .filter(_.attempt(team).isSuccess)
            .reduceOption((oneMission, otherMission) =>
                if (criteria(oneMission.attempt(team).get, otherMission.attempt(team).get))
                    oneMission else otherMission)

    def train(team: Team, missionPickCriteria: MissionPickCriteria): Try[Team] = {
        if missions.isEmpty then return Success(team)

        doBestMission(team, missionPickCriteria)
            .flatMap((missionJustDone, postMissionTeam) =>
                this.copy(missions = missions - missionJustDone).train(postMissionTeam, missionPickCriteria))
    }

    private def doBestMission(team: Team, criteria: MissionPickCriteria): Try[(Mission, Team)] =
        // bestMissionFor garantiza que el equipo puede hacerla
        bestMissionFor(team, criteria)
            .map(mission => Success(mission -> mission.attempt(team).get))
            .getOrElse(Failure(TrainingFailedException("El equipo no puede hacer ninguna de las misiones disponibles")))
}
