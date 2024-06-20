package domain.missions

import cats.syntax.option.*
import domain.{Hero, Ladron, Mago, Team}
import org.scalatest.flatspec.AnyFlatSpec

import scala.util.Success

class TavernTest extends AnyFlatSpec {
    private val emptyTavern: Tavern = Tavern(Set())
    private val emptyTeam: Team = Team("")
    private val dummyMissionPickCriteria = (_: Team, _: Team) => true
    private val getMostGoldPickCriteria = (oneTeam: Team, otherTeam: Team) => oneTeam.earnings > otherTeam.earnings
    private val stealTalismanMission = Mission(List(RobarTalisman))
    private val teamWithMage = Team("", members = Set(Hero(job = Mago.some)))

    "Una taberna" should "tolerar que se le pida una misión aunque no tenga misiones" in {
        assert(emptyTavern.bestMissionFor(emptyTeam, dummyMissionPickCriteria).isEmpty)
    }

    "Una taberna" should "tolerar que se le pida una misión aunque el equipo no pueda hacer ninguna" in {
        assert(Tavern(Set(stealTalismanMission)).bestMissionFor(teamWithMage, dummyMissionPickCriteria).isEmpty)
    }

    "Una taberna" should "sugerir la misión que mejor haga a los héroes de acuerdo a un criterio" in {
        val getLittleGoldMission = Mission(List(PelearContraMonstruo))
        val getMoreGoldMission = Mission(List(PelearContraMonstruo, PelearContraMonstruo))
        val getMostGoldMission = Mission(List(PelearContraMonstruo, PelearContraMonstruo, PelearContraMonstruo))

        val tavern = Tavern(Set(getLittleGoldMission, getMoreGoldMission, getMostGoldMission))

        val bestMission =
            tavern.bestMissionFor(teamWithMage, getMostGoldPickCriteria).get

        assert(bestMission == getMostGoldMission)
    }

    "Una taberna" should "declarar el entrenamiento terminado si ya no tiene misiones" in {
        assert(emptyTavern.train(emptyTeam, dummyMissionPickCriteria) == Success(emptyTeam))
    }

    "Una taberna" should "fallar el entrenamiento si el equipo no puede hacer una tarea" in {
        assert(Tavern(Set(stealTalismanMission)).train(teamWithMage, dummyMissionPickCriteria).isFailure)
    }

    "Una taberna" should "permitir que el equipo sea afectado por las misiones con las que entrena" in {
        // Incluso si el equipo no puede ejecutar todas las misiones inicialmente

        val turnIntoThief = Task(effect = _.changeJob(Ladron.some),
            difficultyRating = (_, _) => Success(1),
            reward = _.getGold(1))
        val turnIntoThiefMission = Mission(List(turnIntoThief))

        val postAllMissionsTeam =
            Tavern(Set(stealTalismanMission, turnIntoThiefMission)).train(teamWithMage, getMostGoldPickCriteria).get
        val postAllMissionsHero = postAllMissionsTeam.members.head

        assert(postAllMissionsHero.job.contains(Ladron))
        assert(postAllMissionsHero.talismans.size == 1)
        assert(postAllMissionsTeam.earnings == 1001)
    }
}