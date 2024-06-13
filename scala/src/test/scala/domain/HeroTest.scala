package domain

import domain.Stat.*
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.immutable.HashMap

class HeroTest extends AnyFlatSpec {
    "Un heroe" should "redondear stats no positivos a 1" in {
        val hero: Hero = Hero(HashMap(Health -> 2, Strength -> 1, Intelligence -> -1))

        assert(hero.stat(Health) === 2)
        assert(hero.stat(Strength) === 1)
        assert(hero.stat(Speed) === 1)
        assert(hero.stat(Intelligence) === 1)
    }

    "Un heroe empleado" should "tener sus stats modificados" in {
        val hero: Hero = Hero(HashMap(Health -> 1, Strength -> 2, Speed -> 3, Intelligence -> 4), Some(Guerrero))

        assert(hero.stat(Health) === 11)
        assert(hero.stat(Strength) === 17)
        assert(hero.stat(Speed) === 3)
        assert(hero.stat(Intelligence) === 1)
    }
}