package domain.missions

import cats.syntax.option.*
import domain.*
import domain.Stat.Speed
import org.scalatest.flatspec.AnyFlatSpec

import scala.util.Success

class MissionTest extends AnyFlatSpec {
    private val teamWithMage = Team("", members = Set(Hero(job = Mago.some)))

    "Una misión" should "ser un éxito si no tiene tareas" in {
        assert(Mission(List()).attempt(Team("")).isSuccess)
    }

    "Una misión" should "fallar si un equipo está vacío" in {
        assert(Mission(List(PelearContraMonstruo)).attempt(Team("")).isFailure)
    }

    "Una misión" should "recompensar al equipo por sus tareas" in {
        assert(Mission(List(PelearContraMonstruo)).attempt(teamWithMage).get.earnings == 100)
    }

    "Una misión" should "fallar si ningún héroe puede cumplir una tarea" in {
        assert(Mission(List(RobarTalisman))
            .attempt(teamWithMage)
            .isFailure)
    }

    "Una misión" should "tomar el héroe mejor capacitado para cada tarea" in {
        val fastUnemployedHero = Hero(StatBlock(Speed -> 100))
        val thief = Hero(job = Ladron.some)
        val team = Team("", members = Set(fastUnemployedHero, thief))
        val teamAfterMission = Mission(List(RobarTalisman)).attempt(team).get

        assert(teamAfterMission.members.filter(_.job.isEmpty).head.talismans.size == 1)
        assert(teamAfterMission.members.filter(_.job.contains(Ladron)).head.talismans.isEmpty)
    }

    "Una misión" should "afectar a los héroes entre tareas" in {
        val turnIntoThief = Task(effect = _.changeJob(Ladron.some),
            difficultyRating = (_, _) => Success(1),
            reward = _.getGold(1))
        val mission = Mission(List(turnIntoThief, RobarTalisman))

        assert(mission.attempt(teamWithMage).isSuccess)
    }
}
