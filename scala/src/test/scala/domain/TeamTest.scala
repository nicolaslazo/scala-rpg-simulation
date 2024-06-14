package domain

import domain.Stat.Speed
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.immutable.HashMap

class TeamTest extends AnyFlatSpec {
    "Un equipo" should "usar un criterio para seleccionar el mejor miembro" in {
        val goodHero: Hero = Hero(HashMap(Speed -> 2))

        assert(Team(name = "", members = List(goodHero, Hero())).bestMember(_.stat(Speed)).get == goodHero)
    }

    "Un equipo" should "decir que no tiene un mejor miembro si está vacío" in {
        assert(Team("").bestMember(_ => 0).isEmpty)
    }
}
