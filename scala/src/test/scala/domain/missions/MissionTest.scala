package domain.missions

import cats.syntax.option.*
import domain.{Hero, Mago, Team}
import org.scalatest.flatspec.AnyFlatSpec

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
}
